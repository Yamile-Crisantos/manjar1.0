package logicaneg;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;
import modelo.Caja;
import modelo.Comanda;
import modelo.Detallecomanda;
import modelo.Establecimiento;
import modelo.Movimientocaja;
import modelo.Usuario;

/**
 * Control de caja (HU-11 y HU-12).
 *
 * Reglas:
 *  - Solo el Dueño o el Gerente de la sucursal operan la caja (HU-11, HU-19). Cada método vuelve a
 *    comprobarlo contra la base de datos con {@link MAcceso}.
 *  - Una sucursal tiene como máximo UNA caja abierta. Para que dos personas no abran dos a la vez (ni
 *    cobren mientras otra hace el corte), las operaciones que cambian la caja bloquean primero la fila de
 *    la sucursal; así se atienden una detrás de otra.
 *  - Al abrir la caja se reinician las porciones del día (HU-05: "se reinicia automáticamente al abrir la caja").
 *  - Los ingresos entran por el cobro de una cuenta (comanda) o de un apartado; los egresos son retiros o
 *    gastos que se escriben a mano. El efectivo que sale nunca puede ser más que el que hay en la caja.
 *  - Arqueo: esperado = monto inicial + ingresos en efectivo - retiros - gastos. La tarjeta y la
 *    transferencia se muestran aparte porque no están en el cajón.
 *  - El corte se hace en dos pasos: primero se ve el resultado con el dinero contado
 *    ({@link #vistaPreviaCorte}) y después se confirma ({@link #cerrar}); el cierre vuelve a calcular todo.
 *  - Una caja cerrada ya no cambia.
 */
@Stateless
@LocalBean
public class MCaja {

    /** Monto máximo de un movimiento o del fondo inicial (la columna es decimal(10,2)). */
    public static final BigDecimal MONTO_MAXIMO = new BigDecimal("1000000.00");
    public static final int CONCEPTO_MAX = 120;

    @PersistenceContext(unitName = "Manjar-ejbPU")
    private EntityManager em;

    @EJB
    private MAcceso mAcceso;

    @EJB
    private MPlatillo mPlatillo;

    @EJB
    private MComanda mComanda;

    // ------------------------------------------------------------------
    // Consulta
    // ------------------------------------------------------------------
    /** La caja abierta de la sucursal, o null si no hay ninguna. */
    public CajaVista abierta(Integer establecimientoId, Integer usuarioId) throws ReglaNegocioException {
        mAcceso.administrador(establecimientoId, usuarioId);
        Caja c = cajaAbierta(establecimientoId);
        return c == null ? null : vista(c);
    }

    public List<MovimientoVista> movimientos(Integer establecimientoId, Integer usuarioId, Integer cajaId)
            throws ReglaNegocioException {
        mAcceso.administrador(establecimientoId, usuarioId);
        Caja c = delNegocio(establecimientoId, cajaId);
        List<Movimientocaja> r = em.createQuery(
                "SELECT m FROM Movimientocaja m WHERE m.cajaid = :c ORDER BY m.fechahora DESC, m.id DESC",
                Movimientocaja.class).setParameter("c", c).getResultList();
        List<MovimientoVista> salida = new ArrayList<>();
        for (Movimientocaja m : r) {
            salida.add(vistaMovimiento(m));
        }
        return salida;
    }

    /** Cajas ya cerradas, la más reciente primero. */
    public List<CajaVista> historial(Integer establecimientoId, Integer usuarioId, int maximo) throws ReglaNegocioException {
        mAcceso.administrador(establecimientoId, usuarioId);
        List<Caja> r = em.createQuery(
                "SELECT c FROM Caja c WHERE c.establecimientoid.id = :e AND c.estado = :cerrada "
                + "ORDER BY c.fechacierre DESC, c.id DESC", Caja.class)
                .setParameter("e", establecimientoId)
                .setParameter("cerrada", Constantes.CAJA_CERRADA)
                .setMaxResults(Math.max(1, Math.min(maximo, 100)))
                .getResultList();
        List<CajaVista> salida = new ArrayList<>();
        for (Caja c : r) {
            salida.add(vista(c));
        }
        return salida;
    }

    public CajaVista buscar(Integer establecimientoId, Integer usuarioId, Integer cajaId) throws ReglaNegocioException {
        mAcceso.administrador(establecimientoId, usuarioId);
        return vista(delNegocio(establecimientoId, cajaId));
    }

    /** Cuentas (comandas) que todavía no se han cobrado ni cancelado, para elegir cuál cobrar. */
    public List<ComandaVista> porCobrar(Integer establecimientoId, Integer usuarioId) throws ReglaNegocioException {
        mAcceso.administrador(establecimientoId, usuarioId);
        return mComanda.porCobrar(establecimientoId);
    }

    // ------------------------------------------------------------------
    // Apertura (HU-11)
    // ------------------------------------------------------------------
    public CajaVista abrir(Integer establecimientoId, Integer usuarioId, BigDecimal montoInicial)
            throws ReglaNegocioException {
        Usuario u = mAcceso.administrador(establecimientoId, usuarioId);
        Establecimiento est = bloquear(establecimientoId);
        BigDecimal inicial = dinero(montoInicial, "El monto inicial", true);
        if (cajaAbierta(establecimientoId) != null) {
            throw new ReglaNegocioException("Ya hay una caja abierta. Haz el corte antes de abrir otra.");
        }
        Caja c = new Caja();
        c.setEstablecimientoid(est);
        c.setUsuarioid(u);
        c.setFechaapertura(new Date());
        c.setMontoinicial(inicial);
        c.setEstado(Constantes.CAJA_ABIERTA);
        em.persist(c);
        em.flush();
        // HU-05: las porciones del día vuelven a su cantidad al abrir la caja
        mPlatillo.reiniciarDia(establecimientoId);
        return vista(c);
    }

    // ------------------------------------------------------------------
    // Movimientos (HU-11)
    // ------------------------------------------------------------------
    /** Cobra una cuenta completa (no se divide entre comensales) y la deja pagada. */
    public MovimientoVista cobrar(Integer establecimientoId, Integer usuarioId, Integer comandaId, String metodo)
            throws ReglaNegocioException {
        Usuario u = mAcceso.administrador(establecimientoId, usuarioId);
        Establecimiento est = bloquear(establecimientoId);
        String m = metodo(metodo);
        Caja caja = cajaAbierta(establecimientoId);
        if (caja == null) {
            throw new ReglaNegocioException("Abre la caja antes de cobrar.");
        }
        Comanda comanda = comanda(establecimientoId, comandaId);
        if (Constantes.COMANDA_CANCELADA.equals(comanda.getEstado())) {
            throw new ReglaNegocioException("Esa comanda está cancelada, no se puede cobrar.");
        }
        if (comanda.getFechacobro() != null) {
            throw new ReglaNegocioException("Esa comanda ya está cobrada.");
        }
        BigDecimal total = totalDe(comanda);
        if (total.signum() <= 0) {
            throw new ReglaNegocioException("La comanda no tiene importe que cobrar.");
        }
        Date ahora = new Date();
        comanda.setFechacobro(ahora);
        Movimientocaja mov = nuevoMovimiento(caja, u, Constantes.MOV_INGRESO, Constantes.CLASE_VENTA, m, total,
                "Cobro de " + etiqueta(comanda), ahora);
        mov.setComandaid(comanda);
        em.persist(mov);
        em.flush();
        return vistaMovimiento(mov);
    }

    /** Retiro de efectivo o gasto pagado desde la caja. */
    public MovimientoVista registrarEgreso(Integer establecimientoId, Integer usuarioId, String clase, BigDecimal monto,
            String concepto) throws ReglaNegocioException {
        Usuario u = mAcceso.administrador(establecimientoId, usuarioId);
        bloquear(establecimientoId);
        String cl = clase == null ? "" : clase.trim().toUpperCase();
        if (!Constantes.CLASE_RETIRO.equals(cl) && !Constantes.CLASE_GASTO.equals(cl)) {
            throw new ReglaNegocioException("Elige si es un retiro o un gasto.");
        }
        BigDecimal importe = dinero(monto, "El monto", false);
        String texto = MComanda.limpiar(concepto);
        if (texto == null) {
            throw new ReglaNegocioException("Escribe el motivo del " + (Constantes.CLASE_RETIRO.equals(cl) ? "retiro." : "gasto."));
        }
        if (texto.length() > CONCEPTO_MAX) {
            throw new ReglaNegocioException("El motivo puede tener hasta " + CONCEPTO_MAX + " caracteres.");
        }
        Caja caja = cajaAbierta(establecimientoId);
        if (caja == null) {
            throw new ReglaNegocioException("Abre la caja para registrar movimientos.");
        }
        BigDecimal hay = vista(caja).getMontoEsperado();
        if (importe.compareTo(hay) > 0) {
            throw new ReglaNegocioException("En la caja solo hay $" + hay.setScale(2, RoundingMode.HALF_UP).toPlainString()
                    + " en efectivo, no se puede sacar más.");
        }
        Movimientocaja mov = nuevoMovimiento(caja, u, Constantes.MOV_EGRESO, cl, Constantes.METODO_EFECTIVO, importe,
                texto, new Date());
        em.persist(mov);
        em.flush();
        return vistaMovimiento(mov);
    }

    /**
     * Para los apartados (HU-13/15/16): el anticipo o el saldo de un apartado entra a la caja abierta de la sucursal.
     * No es público para las pantallas: lo llama MApartado, que ya revisó quién es quién.
     */
    public Movimientocaja ingresoDeApartado(Establecimiento est, Usuario quien, modelo.Apartado apartado,
            String metodo, BigDecimal monto, Date ahora) throws ReglaNegocioException {
        return ingresoDeApartado(est, quien, apartado, metodo, monto, ahora, "Pago del apartado " + apartado.getCodigocomprobante());
    }

    public Movimientocaja ingresoDeApartado(Establecimiento est, Usuario quien, modelo.Apartado apartado,
            String metodo, BigDecimal monto, Date ahora, String concepto) throws ReglaNegocioException {
        Caja caja = cajaAbierta(est.getId());
        if (caja == null) {
            throw new ReglaNegocioException("Abre la caja antes de registrar este pago.");
        }
        Movimientocaja mov = nuevoMovimiento(caja, quien, Constantes.MOV_INGRESO, Constantes.CLASE_APARTADO, metodo(metodo),
                monto, concepto, ahora);
        mov.setApartadoid(apartado);
        em.persist(mov);
        return mov;
    }

    // ------------------------------------------------------------------
    // Corte y arqueo (HU-12)
    // ------------------------------------------------------------------
    /** Paso 1: lo que saldría del corte con el dinero contado, sin cerrar nada. */
    public CajaVista vistaPreviaCorte(Integer establecimientoId, Integer usuarioId, BigDecimal contado)
            throws ReglaNegocioException {
        mAcceso.administrador(establecimientoId, usuarioId);
        Caja caja = cajaAbierta(establecimientoId);
        if (caja == null) {
            throw new ReglaNegocioException("No hay una caja abierta para hacer el corte.");
        }
        BigDecimal monto = dinero(contado, "El dinero contado", true);
        CajaVista v = vista(caja);
        v.setMontoContado(monto);
        v.setDiferencia(monto.subtract(v.getMontoEsperado()));
        return v;
    }

    /**
     * Paso 2: confirma el corte. Vuelve a calcular todo y cierra la caja. Si se manda el efectivo esperado que
     * se vio en la vista previa y entre tanto entró o salió dinero, no cierra: hay que revisar el corte otra vez.
     */
    public CajaVista cerrar(Integer establecimientoId, Integer usuarioId, Integer cajaId, BigDecimal contado,
            BigDecimal esperadoVisto) throws ReglaNegocioException {
        Usuario u = mAcceso.administrador(establecimientoId, usuarioId);
        bloquear(establecimientoId);
        Caja caja = delNegocio(establecimientoId, cajaId);
        if (!Constantes.CAJA_ABIERTA.equals(caja.getEstado())) {
            throw new ReglaNegocioException("Esa caja ya fue cerrada.");
        }
        BigDecimal monto = dinero(contado, "El dinero contado", true);
        CajaVista v = vista(caja);
        if (esperadoVisto != null && esperadoVisto.compareTo(v.getMontoEsperado()) != 0) {
            throw new ReglaNegocioException("Mientras hacías el corte cambió el efectivo esperado (ahora es $"
                    + v.getMontoEsperado().setScale(2, RoundingMode.HALF_UP).toPlainString() + "). Revisa el corte y confírmalo otra vez.");
        }
        caja.setFechacierre(new Date());
        caja.setMontoesperado(v.getMontoEsperado());
        caja.setMontocontado(monto);
        caja.setDiferencia(monto.subtract(v.getMontoEsperado()));
        caja.setEstado(Constantes.CAJA_CERRADA);
        caja.setCerradaporid(u);
        em.flush();
        return vista(caja);
    }

    // ------------------------------------------------------------------
    // Apoyo
    // ------------------------------------------------------------------
    /** Bloquea la fila de la sucursal: las operaciones de caja de esa sucursal se atienden una por una. */
    private Establecimiento bloquear(Integer establecimientoId) throws ReglaNegocioException {
        Establecimiento est = em.find(Establecimiento.class, establecimientoId, LockModeType.PESSIMISTIC_WRITE);
        if (est == null) {
            throw new ReglaNegocioException("No se encontró la sucursal.");
        }
        return est;
    }

    private Caja cajaAbierta(Integer establecimientoId) {
        List<Caja> r = em.createQuery(
                "SELECT c FROM Caja c WHERE c.establecimientoid.id = :e AND c.estado = :abierta ORDER BY c.id DESC", Caja.class)
                .setParameter("e", establecimientoId)
                .setParameter("abierta", Constantes.CAJA_ABIERTA)
                .setMaxResults(1)
                .getResultList();
        return r.isEmpty() ? null : r.get(0);
    }

    private Caja delNegocio(Integer establecimientoId, Integer cajaId) throws ReglaNegocioException {
        Caja c = cajaId == null ? null : em.find(Caja.class, cajaId);
        if (c == null || c.getEstablecimientoid() == null || !c.getEstablecimientoid().getId().equals(establecimientoId)) {
            throw new ReglaNegocioException("Esa caja no es de tu sucursal.");
        }
        return c;
    }

    private Comanda comanda(Integer establecimientoId, Integer comandaId) throws ReglaNegocioException {
        Comanda c = comandaId == null ? null : em.find(Comanda.class, comandaId, LockModeType.PESSIMISTIC_WRITE);
        if (c == null || c.getEstablecimientoid() == null || !c.getEstablecimientoid().getId().equals(establecimientoId)) {
            throw new ReglaNegocioException("Esa comanda no es de tu sucursal.");
        }
        return c;
    }

    private Movimientocaja nuevoMovimiento(Caja caja, Usuario u, String tipo, String clase, String metodo,
            BigDecimal monto, String concepto, Date ahora) {
        Movimientocaja m = new Movimientocaja();
        m.setCajaid(caja);
        m.setUsuarioid(u);
        m.setTipo(tipo);
        m.setClase(clase);
        m.setMetodo(metodo);
        m.setMonto(monto);
        m.setConcepto(concepto);
        m.setFechahora(ahora);
        return m;
    }

    private String metodo(String metodo) throws ReglaNegocioException {
        String m = metodo == null ? "" : metodo.trim().toUpperCase();
        if (!Constantes.METODO_EFECTIVO.equals(m) && !Constantes.METODO_TARJETA.equals(m)
                && !Constantes.METODO_TRANSFERENCIA.equals(m)) {
            throw new ReglaNegocioException("Elige cómo pagó el cliente: efectivo, tarjeta o transferencia.");
        }
        return m;
    }

    private BigDecimal dinero(BigDecimal valor, String nombre, boolean permiteCero) throws ReglaNegocioException {
        if (valor == null) {
            throw new ReglaNegocioException(nombre + " es obligatorio.");
        }
        if (valor.signum() < 0 || (!permiteCero && valor.signum() == 0)) {
            throw new ReglaNegocioException(nombre + (permiteCero ? " no puede ser negativo." : " debe ser mayor que cero."));
        }
        if (valor.compareTo(MONTO_MAXIMO) > 0) {
            throw new ReglaNegocioException(nombre + " no puede ser mayor a $1,000,000.00.");
        }
        if (valor.scale() > 2 && valor.stripTrailingZeros().scale() > 2) {
            throw new ReglaNegocioException(nombre + " solo puede tener centavos (dos decimales).");
        }
        return valor.setScale(2, RoundingMode.HALF_UP);
    }

    static BigDecimal totalDe(Comanda c) {
        BigDecimal total = BigDecimal.ZERO;
        if (c.getDetallecomandaList() != null) {
            for (Detallecomanda d : c.getDetallecomandaList()) {
                BigDecimal precio = d.getPreciounitario() != null ? d.getPreciounitario() : d.getPlatilloid().getPrecio();
                total = total.add(precio.multiply(BigDecimal.valueOf(d.getCantidad())));
            }
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    private String etiqueta(Comanda c) {
        return "comanda " + c.getId() + " (" + ComandaVista.etiqueta(c.getTipo(), MComanda.limpiar(c.getMesa())) + ")";
    }

    /** Totales de una caja: suma los movimientos agrupándolos por tipo y forma de pago. */
    private CajaVista vista(Caja c) {
        CajaVista v = new CajaVista();
        v.setId(c.getId());
        v.setEstado(c.getEstado());
        v.setAbiertaPor(c.getUsuarioid() == null ? "" : c.getUsuarioid().getNombre());
        v.setFechaApertura(c.getFechaapertura());
        v.setFechaCierre(c.getFechacierre());
        v.setMontoInicial(c.getMontoinicial());

        BigDecimal ingEfectivo = BigDecimal.ZERO;
        BigDecimal ingTarjeta = BigDecimal.ZERO;
        BigDecimal ingTransf = BigDecimal.ZERO;
        BigDecimal retiros = BigDecimal.ZERO;
        BigDecimal gastos = BigDecimal.ZERO;
        int cuantos = 0;
        List<Object[]> filas = em.createQuery(
                "SELECT m.tipo, m.clase, m.metodo, SUM(m.monto), COUNT(m) FROM Movimientocaja m WHERE m.cajaid = :c "
                + "GROUP BY m.tipo, m.clase, m.metodo", Object[].class).setParameter("c", c).getResultList();
        for (Object[] f : filas) {
            String tipo = (String) f[0];
            String clase = (String) f[1];
            String metodo = f[2] == null ? Constantes.METODO_EFECTIVO : (String) f[2];
            BigDecimal suma = (BigDecimal) f[3];
            cuantos += ((Number) f[4]).intValue();
            if (Constantes.MOV_INGRESO.equals(tipo)) {
                if (Constantes.METODO_TARJETA.equals(metodo)) {
                    ingTarjeta = ingTarjeta.add(suma);
                } else if (Constantes.METODO_TRANSFERENCIA.equals(metodo)) {
                    ingTransf = ingTransf.add(suma);
                } else {
                    ingEfectivo = ingEfectivo.add(suma);
                }
            } else if (Constantes.CLASE_RETIRO.equals(clase)) {
                retiros = retiros.add(suma);
            } else {
                gastos = gastos.add(suma);
            }
        }
        v.setIngresosEfectivo(ingEfectivo);
        v.setIngresosTarjeta(ingTarjeta);
        v.setIngresosTransferencia(ingTransf);
        v.setRetiros(retiros);
        v.setGastos(gastos);
        v.setMovimientos(cuantos);
        if (Constantes.CAJA_CERRADA.equals(c.getEstado()) && c.getMontoesperado() != null) {
            v.setMontoEsperado(c.getMontoesperado());
            v.setMontoContado(c.getMontocontado());
            v.setDiferencia(c.getDiferencia());
        } else {
            v.setMontoEsperado(v.getMontoInicial().add(ingEfectivo).subtract(retiros).subtract(gastos));
            Number pendientes = (Number) em.createQuery(
                    "SELECT COUNT(x) FROM Comanda x WHERE x.establecimientoid.id = :e AND x.estado <> :cancelada "
                    + "AND x.fechacobro IS NULL").setParameter("e", c.getEstablecimientoid().getId())
                    .setParameter("cancelada", Constantes.COMANDA_CANCELADA).getSingleResult();
            v.setPendientesDeCobro(pendientes.intValue());
        }
        return v;
    }

    private MovimientoVista vistaMovimiento(Movimientocaja m) {
        MovimientoVista v = new MovimientoVista();
        v.setId(m.getId());
        v.setTipo(m.getTipo());
        v.setClase(m.getClase());
        v.setMetodo(m.getMetodo());
        v.setMonto(m.getMonto());
        v.setConcepto(m.getConcepto());
        v.setFechaHora(m.getFechahora());
        v.setUsuarioNombre(m.getUsuarioid() == null ? "" : m.getUsuarioid().getNombre());
        v.setComandaId(m.getComandaid() == null ? null : m.getComandaid().getId());
        v.setApartadoId(m.getApartadoid() == null ? null : m.getApartadoid().getId());
        return v;
    }
}
