package logicaneg;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;
import modelo.Apartado;
import modelo.Detalleapartado;
import modelo.Establecimiento;
import modelo.Notificacion;
import modelo.Pago;
import modelo.Platillo;
import modelo.Usuario;

/**
 * Apartados y pago anticipado (HU-13, HU-15, HU-16 y HU-17). La pasarela de pagos en línea (HU-14) NO se
 * incluye porque el proveedor todavía no está definido (ver la observación de HU-14): el cliente aparta y el
 * negocio registra el pago cuando lo recibe (efectivo, tarjeta con su terminal o transferencia).
 *
 * Flujo de un apartado:
 *   PENDIENTE (porciones retenidas, esperando el anticipo) -> PAGADO -> LISTO -> ENTREGADO
 *   y, aparte, CANCELADO (el cliente o el negocio, solo antes de pagar) o VENCIDO (no se pagó a tiempo).
 *
 * Pago con anticipo: al apartar el cliente solo paga el {@link #ANTICIPO_PCT} % del total (el anticipo). El
 * resto (el saldo) se cobra en la caja al entregar el pedido, con el código del comprobante. Si el porcentaje
 * es 100 el apartado se paga completo al apartar, como antes.
 *
 * Reglas:
 *  - Solo un cliente crea apartados, y solo ve y cancela los suyos. El Dueño o el Gerente de la sucursal
 *    registran el pago, marcan "listo" y entregan; cada método vuelve a comprobar sucursal y rol.
 *  - Al apartar se descuentan las porciones del día (HU-05); si se cancela o vence, regresan.
 *  - Un apartado sin pago se libera solo a los {@link #RETENCION_MIN} minutos. Se revisa cada vez que alguien
 *    consulta apartados, no hace falta un proceso aparte.
 *  - Se recoge hoy, con al menos {@link #ANTICIPACION_MIN} minutos de anticipación.
 *  - El pago se registra en la caja abierta de la sucursal (HU-11), así el corte lo incluye.
 *  - La entrega pide el código del comprobante y debe coincidir.
 *  - Después de pagar el anticipo ya no se cancela (no hay reembolsos en esta versión).
 *  - Entregar exige la caja abierta si queda saldo, porque ese cobro entra a la caja de ese día.
 *  - El apartado puede ser para llevar o para comer en el negocio; en ambos casos no se asigna mesa.
 *  - Cada cambio importante deja un aviso al cliente (HU-17), dentro de la aplicación.
 */
@Stateless
@LocalBean
public class MApartado {

    /** Minutos que se retienen las porciones esperando el pago (se puede cambiar con -Dmanjar.apartado.retencion.min). */
    public static final int RETENCION_MIN = Integer.getInteger("manjar.apartado.retencion.min", 30);
    /** Minutos mínimos entre ahora y la hora de recolección. */
    public static final int ANTICIPACION_MIN = Integer.getInteger("manjar.apartado.anticipacion.min", 15);
    public static final int MAX_PENDIENTES = 3;
    public static final int MAX_LINEAS = 20;
    public static final int MAX_CANTIDAD = 99;
    public static final int REFERENCIA_MAX = 80;
    /** Porcentaje del total que se paga al apartar (se puede cambiar con -Dmanjar.apartado.anticipo.pct, de 1 a 100). */
    public static final int ANTICIPO_PCT = Math.max(1, Math.min(100, Integer.getInteger("manjar.apartado.anticipo.pct", 50)));

    private static final String ALFABETO = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
    private static final SecureRandom AZAR = new SecureRandom();

    @PersistenceContext(unitName = "Manjar-ejbPU")
    private EntityManager em;

    @EJB
    private MAcceso mAcceso;

    @EJB
    private MPlatillo mPlatillo;

    @EJB
    private MCaja mCaja;

    // ------------------------------------------------------------------
    // Cliente (HU-13, HU-17)
    // ------------------------------------------------------------------
    /** Aparta platillos para llevar (la forma de siempre). */
    public ApartadoVista crear(Integer clienteId, Integer establecimientoId, List<LineaPedido> pedido, Date horaRecoleccion)
            throws ReglaNegocioException {
        return crear(clienteId, establecimientoId, pedido, horaRecoleccion, Constantes.ENTREGA_LLEVAR);
    }

    /**
     * Aparta platillos de un negocio para recogerlos hoy a la hora indicada.
     *
     * @param tipoEntrega LLEVAR (se recoge para llevar) o AQUI (se come en el negocio; no se asigna mesa)
     */
    public ApartadoVista crear(Integer clienteId, Integer establecimientoId, List<LineaPedido> pedido, Date horaRecoleccion,
            String tipoEntrega) throws ReglaNegocioException {
        String entrega = tipoEntrega == null ? "" : tipoEntrega.trim().toUpperCase();
        if (!Constantes.ENTREGA_LLEVAR.equals(entrega) && !Constantes.ENTREGA_AQUI.equals(entrega)) {
            throw new ReglaNegocioException("Elige si lo recoges para llevar o lo comes en el negocio.");
        }
        Usuario cliente = mAcceso.cliente(clienteId);
        Establecimiento est = mAcceso.establecimiento(establecimientoId);
        Date ahora = new Date();
        validarHora(horaRecoleccion, ahora);
        Map<Integer, Integer> porPlatillo = unir(pedido);

        expirarVencidos(null, clienteId);
        Number abiertos = (Number) em.createQuery(
                "SELECT COUNT(a) FROM Apartado a WHERE a.clienteid.id = :c AND a.estado = :p")
                .setParameter("c", clienteId).setParameter("p", Constantes.APARTADO_PENDIENTE).getSingleResult();
        if (abiertos.intValue() >= MAX_PENDIENTES) {
            throw new ReglaNegocioException("Ya tienes " + MAX_PENDIENTES + " apartados esperando pago. "
                    + "Cancela alguno o espera a que el negocio confirme tu pago.");
        }

        // Se descuentan en orden de id para no cruzar bloqueos con otros clientes o con el mesero
        Map<Integer, Platillo> platillos = new TreeMap<>();
        for (Map.Entry<Integer, Integer> e : porPlatillo.entrySet()) {
            platillos.put(e.getKey(), mPlatillo.descontar(establecimientoId, e.getKey(), e.getValue()));
        }

        Apartado a = new Apartado();
        a.setClienteid(cliente);
        a.setEstablecimientoid(est);
        a.setFechacreacion(ahora);
        a.setHorarecoleccion(horaRecoleccion);
        a.setEstado(Constantes.APARTADO_PENDIENTE);
        a.setCodigocomprobante(nuevoCodigo());
        a.setTipoentrega(entrega);
        BigDecimal total = BigDecimal.ZERO;
        List<Detalleapartado> detalles = new ArrayList<>();
        for (Map.Entry<Integer, Integer> e : porPlatillo.entrySet()) {
            Platillo p = platillos.get(e.getKey());
            Detalleapartado d = new Detalleapartado();
            d.setApartadoid(a);
            d.setPlatilloid(p);
            d.setCantidad(e.getValue());
            d.setPreciounitario(p.getPrecio());
            d.setCostounitario(p.getCosto());
            detalles.add(d);
            total = total.add(p.getPrecio().multiply(BigDecimal.valueOf(e.getValue())));
        }
        a.setDetalleapartadoList(detalles);
        a.setTotal(total.setScale(2, RoundingMode.HALF_UP));
        a.setAnticipo(anticipoDe(a.getTotal()));
        em.persist(a);
        em.flush();
        return vista(a, ahora);
    }

    /** Los apartados del cliente (de todos los negocios), el más reciente primero. */
    public List<ApartadoVista> delCliente(Integer clienteId) throws ReglaNegocioException {
        mAcceso.cliente(clienteId);
        expirarVencidos(null, clienteId);
        List<Apartado> r = em.createQuery(
                "SELECT a FROM Apartado a WHERE a.clienteid.id = :c ORDER BY a.fechacreacion DESC, a.id DESC", Apartado.class)
                .setParameter("c", clienteId).setMaxResults(50).getResultList();
        return vistas(r);
    }

    /**
     * Los negocios donde el cliente ya apartó (el más reciente primero): su atajo para volver a apartar sin
     * escanear el QR otra vez. No es un directorio: solo aparecen negocios donde esa persona ya tiene un apartado.
     */
    public List<NegocioVista> negociosDelCliente(Integer clienteId) throws ReglaNegocioException {
        mAcceso.cliente(clienteId);
        List<Object[]> filas = em.createQuery(
                "SELECT a.establecimientoid.id, MAX(a.fechacreacion), COUNT(a) FROM Apartado a WHERE a.clienteid.id = :c "
                + "GROUP BY a.establecimientoid.id ORDER BY MAX(a.fechacreacion) DESC", Object[].class)
                .setParameter("c", clienteId).setMaxResults(8).getResultList();
        List<NegocioVista> salida = new ArrayList<>();
        for (Object[] f : filas) {
            Establecimiento e = em.find(Establecimiento.class, (Integer) f[0]);
            if (e != null && e.getCodigoQR() != null) {
                salida.add(new NegocioVista(e.getId(), e.getNombre(), e.getCodigoQR(), ((Number) f[2]).intValue()));
            }
        }
        return salida;
    }

    /** El cliente cancela un apartado suyo que todavía no se paga. */
    public ApartadoVista cancelarComoCliente(Integer clienteId, Integer apartadoId) throws ReglaNegocioException {
        mAcceso.cliente(clienteId);
        Apartado a = apartadoBloqueado(apartadoId);
        if (a.getClienteid() == null || !a.getClienteid().getId().equals(clienteId)) {
            throw new ReglaNegocioException("Ese apartado no es tuyo.");
        }
        cancelarPendiente(a, false);
        return vista(a, new Date());
    }

    public List<NotificacionVista> notificaciones(Integer clienteId) throws ReglaNegocioException {
        mAcceso.cliente(clienteId);
        List<Notificacion> r = em.createQuery(
                "SELECT n FROM Notificacion n WHERE n.apartadoid.clienteid.id = :c ORDER BY n.fecha DESC, n.id DESC",
                Notificacion.class).setParameter("c", clienteId).setMaxResults(30).getResultList();
        List<NotificacionVista> salida = new ArrayList<>();
        for (Notificacion n : r) {
            NotificacionVista v = new NotificacionVista();
            v.setId(n.getId());
            v.setApartadoId(n.getApartadoid().getId());
            v.setTipo(n.getTipo());
            v.setMensaje(n.getMensaje());
            v.setNegocioNombre(n.getApartadoid().getEstablecimientoid().getNombre());
            v.setFecha(n.getFecha());
            v.setLeida(Constantes.NOTIF_LEIDA.equals(n.getEstado()));
            salida.add(v);
        }
        return salida;
    }

    public int noLeidas(Integer clienteId) throws ReglaNegocioException {
        mAcceso.cliente(clienteId);
        Number n = (Number) em.createQuery(
                "SELECT COUNT(n) FROM Notificacion n WHERE n.apartadoid.clienteid.id = :c AND n.estado <> :leida")
                .setParameter("c", clienteId).setParameter("leida", Constantes.NOTIF_LEIDA).getSingleResult();
        return n.intValue();
    }

    public void marcarLeida(Integer clienteId, Integer notificacionId) throws ReglaNegocioException {
        mAcceso.cliente(clienteId);
        Notificacion n = notificacionId == null ? null : em.find(Notificacion.class, notificacionId);
        if (n == null || n.getApartadoid().getClienteid() == null || !n.getApartadoid().getClienteid().getId().equals(clienteId)) {
            throw new ReglaNegocioException("No se encontró ese aviso.");
        }
        n.setEstado(Constantes.NOTIF_LEIDA);
    }

    public void marcarTodasLeidas(Integer clienteId) throws ReglaNegocioException {
        mAcceso.cliente(clienteId);
        em.createQuery("UPDATE Notificacion n SET n.estado = :leida WHERE n.estado <> :leida AND n.apartadoid IN "
                + "(SELECT a FROM Apartado a WHERE a.clienteid.id = :c)")
                .setParameter("leida", Constantes.NOTIF_LEIDA).setParameter("c", clienteId).executeUpdate();
    }

    // ------------------------------------------------------------------
    // Negocio (HU-15, HU-16, HU-17)
    // ------------------------------------------------------------------
    /** Apartados en proceso de la sucursal (esperando pago, pagados y listos), por hora de recolección. */
    public List<ApartadoVista> activos(Integer establecimientoId, Integer usuarioId) throws ReglaNegocioException {
        mAcceso.administrador(establecimientoId, usuarioId);
        expirarVencidos(establecimientoId, null);
        List<Apartado> r = em.createQuery(
                "SELECT a FROM Apartado a WHERE a.establecimientoid.id = :e AND a.estado IN (:p, :g, :l) "
                + "ORDER BY a.horarecoleccion, a.id", Apartado.class)
                .setParameter("e", establecimientoId)
                .setParameter("p", Constantes.APARTADO_PENDIENTE)
                .setParameter("g", Constantes.APARTADO_PAGADO)
                .setParameter("l", Constantes.APARTADO_LISTO)
                .getResultList();
        return vistas(r);
    }

    /** Apartados que terminaron hoy (entregados, cancelados o vencidos). */
    public List<ApartadoVista> terminadosHoy(Integer establecimientoId, Integer usuarioId) throws ReglaNegocioException {
        mAcceso.administrador(establecimientoId, usuarioId);
        Date inicio = MComanda.inicioDelDia(new Date());
        List<Apartado> r = em.createQuery(
                "SELECT a FROM Apartado a WHERE a.establecimientoid.id = :e AND a.estado IN (:g, :c, :v) "
                + "AND (a.fechacreacion >= :i OR a.fechaentrega >= :i) ORDER BY a.id DESC", Apartado.class)
                .setParameter("e", establecimientoId)
                .setParameter("g", Constantes.APARTADO_ENTREGADO)
                .setParameter("c", Constantes.APARTADO_CANCELADO)
                .setParameter("v", Constantes.APARTADO_VENCIDO)
                .setParameter("i", inicio)
                .getResultList();
        return vistas(r);
    }

    /** El negocio recibió el anticipo: se registra y el apartado queda pagado, en preparación (HU-13). */
    public ApartadoVista registrarPago(Integer establecimientoId, Integer usuarioId, Integer apartadoId, String metodo,
            String referencia) throws ReglaNegocioException {
        Usuario u = mAcceso.administrador(establecimientoId, usuarioId);
        Establecimiento est = em.find(Establecimiento.class, establecimientoId, LockModeType.PESSIMISTIC_WRITE);
        Apartado a = delNegocio(establecimientoId, apartadoId, true);
        Date ahora = new Date();
        if (Constantes.APARTADO_VENCIDO.equals(a.getEstado()) || estaVencido(a, ahora)) {
            throw new ReglaNegocioException("Este apartado venció sin pago y las porciones regresaron al menú.");
        }
        if (!Constantes.APARTADO_PENDIENTE.equals(a.getEstado())) {
            throw new ReglaNegocioException(Constantes.APARTADO_CANCELADO.equals(a.getEstado())
                    ? "Este apartado está cancelado." : "Este apartado ya tiene su pago registrado.");
        }
        String m = metodo == null ? "" : metodo.trim().toUpperCase();
        if (!Constantes.METODO_EFECTIVO.equals(m) && !Constantes.METODO_TARJETA.equals(m)
                && !Constantes.METODO_TRANSFERENCIA.equals(m)) {
            throw new ReglaNegocioException("Elige cómo pagó el cliente: efectivo, tarjeta o transferencia.");
        }
        String ref = MComanda.limpiar(referencia);
        if (Constantes.METODO_TRANSFERENCIA.equals(m) && ref == null) {
            throw new ReglaNegocioException("Escribe la referencia o clave de rastreo de la transferencia.");
        }
        if (ref != null && ref.length() > REFERENCIA_MAX) {
            throw new ReglaNegocioException("La referencia puede tener hasta " + REFERENCIA_MAX + " caracteres.");
        }
        // Primero la caja (si está cerrada se detiene aquí y no queda nada a medias)
        BigDecimal monto = a.getAnticipo();
        mCaja.ingresoDeApartado(est, u, a, m, monto, ahora, "Anticipo del apartado " + a.getCodigocomprobante());
        Pago p = new Pago();
        p.setApartadoid(a);
        p.setMonto(monto);
        p.setConcepto(Constantes.PAGO_ANTICIPO);
        p.setMetodo(m);
        p.setReferencia(ref);
        p.setEstado(Constantes.PAGO_CONFIRMADO);
        p.setFecha(ahora);
        p.setRegistradoporid(u);
        em.persist(p);
        if (a.getPagoList() != null) {
            a.getPagoList().add(p);
        }
        a.setEstado(Constantes.APARTADO_PAGADO);
        BigDecimal saldo = saldoDe(a);
        notificar(a, Constantes.NOTIF_PAGO, (saldo.signum() > 0 ? "Recibimos tu anticipo de $" : "Recibimos tu pago de $")
                + monto.setScale(2, RoundingMode.HALF_UP).toPlainString() + ". Tu apartado "
                + codigoVisible(a) + " en " + est.getNombre() + " está confirmado; te avisaremos cuando esté listo."
                + (saldo.signum() > 0 ? " El resto ($" + saldo.setScale(2, RoundingMode.HALF_UP).toPlainString()
                        + ") se paga al recogerlo." : ""), ahora);
        em.flush();
        return vista(a, ahora);
    }

    /** La cocina terminó: el apartado queda listo y se avisa al cliente (HU-17). */
    public ApartadoVista marcarListo(Integer establecimientoId, Integer usuarioId, Integer apartadoId)
            throws ReglaNegocioException {
        mAcceso.administrador(establecimientoId, usuarioId);
        Apartado a = delNegocio(establecimientoId, apartadoId, true);
        if (!Constantes.APARTADO_PAGADO.equals(a.getEstado())) {
            throw new ReglaNegocioException(Constantes.APARTADO_LISTO.equals(a.getEstado())
                    ? "Este apartado ya está marcado como listo."
                    : "Solo se puede marcar como listo un apartado ya pagado.");
        }
        Date ahora = new Date();
        a.setEstado(Constantes.APARTADO_LISTO);
        notificar(a, Constantes.NOTIF_LISTO, "¡Tu pedido " + codigoVisible(a) + " ya está listo! Pasa a recogerlo a "
                + a.getEstablecimientoid().getNombre() + ".", ahora);
        em.flush();
        return vista(a, ahora);
    }

    /** Entrega un apartado que ya no debe saldo (compatibilidad). */
    public ApartadoVista entregar(Integer establecimientoId, Integer usuarioId, Integer apartadoId, String codigo)
            throws ReglaNegocioException {
        return entregar(establecimientoId, usuarioId, apartadoId, codigo, null, null);
    }

    /**
     * Entrega el pedido al cliente (HU-16). Hay que escribir el código del comprobante y debe coincidir. Si queda
     * saldo por cobrar, se cobra en ese momento con el método indicado y entra a la caja abierta.
     */
    public ApartadoVista entregar(Integer establecimientoId, Integer usuarioId, Integer apartadoId, String codigo,
            String metodoSaldo, String referenciaSaldo) throws ReglaNegocioException {
        Usuario u = mAcceso.administrador(establecimientoId, usuarioId);
        // Misma secuencia de bloqueos que registrarPago (primero la sucursal, luego el apartado): sin interbloqueos
        Establecimiento est = em.find(Establecimiento.class, establecimientoId, LockModeType.PESSIMISTIC_WRITE);
        Apartado a = delNegocio(establecimientoId, apartadoId, true);
        if (!Constantes.APARTADO_LISTO.equals(a.getEstado())) {
            throw new ReglaNegocioException(Constantes.APARTADO_ENTREGADO.equals(a.getEstado())
                    ? "Este apartado ya fue entregado." : "Solo se entrega un apartado que ya está listo.");
        }
        String escrito = normalizarCodigo(codigo);
        if (escrito.isEmpty()) {
            throw new ReglaNegocioException("Escribe el código del comprobante del cliente.");
        }
        if (!escrito.equals(a.getCodigocomprobante())) {
            throw new ReglaNegocioException("El código no coincide con el de este apartado.");
        }
        Date ahora = new Date();
        BigDecimal saldo = saldoDe(a);
        if (saldo.signum() > 0) {
            String m = metodoSaldo == null ? "" : metodoSaldo.trim().toUpperCase();
            if (!Constantes.METODO_EFECTIVO.equals(m) && !Constantes.METODO_TARJETA.equals(m)
                    && !Constantes.METODO_TRANSFERENCIA.equals(m)) {
                throw new ReglaNegocioException("Elige cómo paga el resto ($" + saldo.toPlainString()
                        + "): efectivo, tarjeta o transferencia.");
            }
            String ref = MComanda.limpiar(referenciaSaldo);
            if (Constantes.METODO_TRANSFERENCIA.equals(m) && ref == null) {
                throw new ReglaNegocioException("Escribe la referencia o clave de rastreo de la transferencia.");
            }
            if (ref != null && ref.length() > REFERENCIA_MAX) {
                throw new ReglaNegocioException("La referencia puede tener hasta " + REFERENCIA_MAX + " caracteres.");
            }
            // Primero la caja: si está cerrada se detiene aquí y no queda nada a medias
            mCaja.ingresoDeApartado(est, u, a, m, saldo, ahora, "Saldo del apartado " + a.getCodigocomprobante());
            Pago p = new Pago();
            p.setApartadoid(a);
            p.setMonto(saldo);
            p.setConcepto(Constantes.PAGO_SALDO);
            p.setMetodo(m);
            p.setReferencia(ref);
            p.setEstado(Constantes.PAGO_CONFIRMADO);
            p.setFecha(ahora);
            p.setRegistradoporid(u);
            em.persist(p);
            if (a.getPagoList() != null) {
                a.getPagoList().add(p);
            }
        }
        a.setEstado(Constantes.APARTADO_ENTREGADO);
        a.setFechaentrega(ahora);
        a.setEntregadoporid(u);
        em.flush();
        return vista(a, ahora);
    }

    /** El negocio cancela un apartado que todavía no se ha pagado. */
    public ApartadoVista cancelarComoNegocio(Integer establecimientoId, Integer usuarioId, Integer apartadoId)
            throws ReglaNegocioException {
        mAcceso.administrador(establecimientoId, usuarioId);
        Apartado a = delNegocio(establecimientoId, apartadoId, true);
        cancelarPendiente(a, true);
        return vista(a, new Date());
    }

    // ------------------------------------------------------------------
    // Apoyo
    // ------------------------------------------------------------------
    private void cancelarPendiente(Apartado a, boolean porNegocio) throws ReglaNegocioException {
        if (!Constantes.APARTADO_PENDIENTE.equals(a.getEstado())) {
            throw new ReglaNegocioException(Constantes.APARTADO_CANCELADO.equals(a.getEstado())
                    ? "Este apartado ya está cancelado."
                    : "Solo se puede cancelar un apartado que todavía no se paga.");
        }
        devolverPorciones(a);
        a.setEstado(Constantes.APARTADO_CANCELADO);
        if (porNegocio) {
            notificar(a, Constantes.NOTIF_CANCELADO, "El negocio canceló tu apartado " + codigoVisible(a)
                    + " antes de pagarlo. Las porciones regresaron al menú.", new Date());
        }
    }

    private void devolverPorciones(Apartado a) throws ReglaNegocioException {
        Map<Integer, Integer> porPlatillo = new TreeMap<>();
        for (Detalleapartado d : a.getDetalleapartadoList()) {
            Integer id = d.getPlatilloid().getId();
            Integer antes = porPlatillo.get(id);
            porPlatillo.put(id, (antes == null ? 0 : antes) + d.getCantidad());
        }
        for (Map.Entry<Integer, Integer> e : porPlatillo.entrySet()) {
            mPlatillo.devolver(a.getEstablecimientoid().getId(), e.getKey(), e.getValue());
        }
    }

    /** Libera los apartados sin pago que ya pasaron su tiempo (de una sucursal, de un cliente, o de ambos filtros). */
    private void expirarVencidos(Integer establecimientoId, Integer clienteId) {
        Date limite = new Date(System.currentTimeMillis() - RETENCION_MIN * 60000L);
        StringBuilder jpql = new StringBuilder(
                "SELECT a FROM Apartado a WHERE a.estado = :p AND a.fechacreacion < :limite");
        if (establecimientoId != null) {
            jpql.append(" AND a.establecimientoid.id = :e");
        }
        if (clienteId != null) {
            jpql.append(" AND a.clienteid.id = :c");
        }
        javax.persistence.TypedQuery<Apartado> q = em.createQuery(jpql.toString(), Apartado.class)
                .setParameter("p", Constantes.APARTADO_PENDIENTE).setParameter("limite", limite);
        if (establecimientoId != null) {
            q.setParameter("e", establecimientoId);
        }
        if (clienteId != null) {
            q.setParameter("c", clienteId);
        }
        for (Apartado a : q.getResultList()) {
            em.refresh(a, LockModeType.PESSIMISTIC_WRITE);
            if (!Constantes.APARTADO_PENDIENTE.equals(a.getEstado())) {
                continue; // alguien lo pagó o canceló mientras tanto
            }
            try {
                devolverPorciones(a);
            } catch (ReglaNegocioException e) {
                continue; // el platillo ya no existe en el menú: no hay porciones que devolver
            }
            a.setEstado(Constantes.APARTADO_VENCIDO);
            notificar(a, Constantes.NOTIF_VENCIDO, "Tu apartado " + codigoVisible(a)
                    + " venció porque no se registró el pago a tiempo. Las porciones regresaron al menú; puedes apartar de nuevo.",
                    new Date());
        }
    }

    private void notificar(Apartado a, String tipo, String mensaje, Date fecha) {
        Notificacion n = new Notificacion();
        n.setApartadoid(a);
        n.setTipo(tipo);
        n.setMensaje(mensaje);
        n.setEstado(Constantes.NOTIF_NUEVA);
        n.setFecha(fecha);
        em.persist(n);
    }

    private Apartado apartadoBloqueado(Integer apartadoId) throws ReglaNegocioException {
        Apartado a = apartadoId == null ? null : em.find(Apartado.class, apartadoId, LockModeType.PESSIMISTIC_WRITE);
        if (a == null) {
            throw new ReglaNegocioException("No se encontró ese apartado.");
        }
        return a;
    }

    private Apartado delNegocio(Integer establecimientoId, Integer apartadoId, boolean bloquear) throws ReglaNegocioException {
        Apartado a = apartadoId == null ? null
                : (bloquear ? em.find(Apartado.class, apartadoId, LockModeType.PESSIMISTIC_WRITE) : em.find(Apartado.class, apartadoId));
        if (a == null || a.getEstablecimientoid() == null || !a.getEstablecimientoid().getId().equals(establecimientoId)) {
            throw new ReglaNegocioException("Ese apartado no es de tu sucursal.");
        }
        return a;
    }

    private boolean estaVencido(Apartado a, Date ahora) {
        return Constantes.APARTADO_PENDIENTE.equals(a.getEstado()) && a.getFechacreacion() != null
                && a.getFechacreacion().getTime() < ahora.getTime() - RETENCION_MIN * 60000L;
    }

    private void validarHora(Date hora, Date ahora) throws ReglaNegocioException {
        if (hora == null) {
            throw new ReglaNegocioException("Elige a qué hora vas a recoger tu pedido.");
        }
        // Un minuto de tolerancia, por el tiempo que tarda en llegar la petición
        if (hora.getTime() < ahora.getTime() + (ANTICIPACION_MIN - 1) * 60000L) {
            throw new ReglaNegocioException("Elige una hora con al menos " + ANTICIPACION_MIN + " minutos de anticipación.");
        }
        if (!MComanda.inicioDelDia(hora).equals(MComanda.inicioDelDia(ahora))) {
            throw new ReglaNegocioException("Solo se puede apartar para recoger hoy.");
        }
    }

    private Map<Integer, Integer> unir(List<LineaPedido> pedido) throws ReglaNegocioException {
        if (pedido == null || pedido.isEmpty()) {
            throw new ReglaNegocioException("Elige al menos un platillo para apartar.");
        }
        Map<Integer, Integer> r = new TreeMap<>();
        for (LineaPedido l : pedido) {
            if (l == null || l.getPlatilloId() == null) {
                throw new ReglaNegocioException("Hay un platillo sin elegir en el apartado.");
            }
            if (l.getCantidad() < 1 || l.getCantidad() > MAX_CANTIDAD) {
                throw new ReglaNegocioException("La cantidad de cada platillo debe estar entre 1 y " + MAX_CANTIDAD + ".");
            }
            Integer antes = r.get(l.getPlatilloId());
            int suma = (antes == null ? 0 : antes) + l.getCantidad();
            if (suma > MAX_CANTIDAD) {
                throw new ReglaNegocioException("Máximo " + MAX_CANTIDAD + " porciones de un mismo platillo.");
            }
            r.put(l.getPlatilloId(), suma);
        }
        if (r.size() > MAX_LINEAS) {
            throw new ReglaNegocioException("Máximo " + MAX_LINEAS + " platillos distintos por apartado.");
        }
        return r;
    }

    /** 6 caracteres sin letras que se confunden (0/O, 1/I): se lee fácil en el celular y se dicta sin dudas. */
    private String nuevoCodigo() throws ReglaNegocioException {
        for (int intento = 0; intento < 25; intento++) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 6; i++) {
                sb.append(ALFABETO.charAt(AZAR.nextInt(ALFABETO.length())));
            }
            Number existe = (Number) em.createQuery("SELECT COUNT(a) FROM Apartado a WHERE a.codigocomprobante = :c")
                    .setParameter("c", sb.toString()).getSingleResult();
            if (existe.intValue() == 0) {
                return sb.toString();
            }
        }
        throw new ReglaNegocioException("No se pudo generar el comprobante. Intenta de nuevo.");
    }

    /** Quita espacios y guiones y pasa a mayúsculas: el cliente puede escribir "k7m-2qx" o "K7M 2QX". */
    static String normalizarCodigo(String codigo) {
        if (codigo == null) {
            return "";
        }
        return codigo.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
    }

    /** El anticipo de un total: el {@link #ANTICIPO_PCT} %, a centavos (al medio centavo se sube). */
    public static BigDecimal anticipoDe(BigDecimal total) {
        BigDecimal t = total.setScale(2, RoundingMode.HALF_UP);
        if (ANTICIPO_PCT >= 100) {
            return t;
        }
        BigDecimal a = t.multiply(BigDecimal.valueOf(ANTICIPO_PCT)).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        if (a.signum() == 0 && t.signum() > 0) {
            a = new BigDecimal("0.01");
        }
        return a;
    }

    /** Lo que se cobra al entregar: el total menos el anticipo. */
    private BigDecimal saldoDe(Apartado a) {
        BigDecimal anticipo = a.getAnticipo() == null ? a.getTotal() : a.getAnticipo();
        BigDecimal debe = a.getTotal().subtract(anticipo);
        return debe.signum() < 0 ? BigDecimal.ZERO : debe.setScale(2, RoundingMode.HALF_UP);
    }

    private String codigoVisible(Apartado a) {
        String c = a.getCodigocomprobante();
        return c.length() > 3 ? c.substring(0, 3) + "-" + c.substring(3) : c;
    }

    private List<ApartadoVista> vistas(List<Apartado> apartados) {
        Date ahora = new Date();
        List<ApartadoVista> r = new ArrayList<>();
        for (Apartado a : apartados) {
            r.add(vista(a, ahora));
        }
        return r;
    }

    private ApartadoVista vista(Apartado a, Date ahora) {
        ApartadoVista v = new ApartadoVista();
        v.setId(a.getId());
        v.setCodigo(a.getCodigocomprobante());
        v.setEstado(a.getEstado());
        v.setNegocioId(a.getEstablecimientoid().getId());
        v.setNegocioNombre(a.getEstablecimientoid().getNombre());
        v.setClienteNombre(a.getClienteid() == null ? "" : a.getClienteid().getNombre());
        v.setFechaCreacion(a.getFechacreacion());
        v.setHoraRecoleccion(a.getHorarecoleccion());
        v.setTotal(a.getTotal());
        v.setAnticipo(a.getAnticipo());
        v.setTipoEntrega(a.getTipoentrega());
        v.setFechaEntrega(a.getFechaentrega());
        v.setEntregadoPor(a.getEntregadoporid() == null ? null : a.getEntregadoporid().getNombre());
        if (Constantes.APARTADO_PENDIENTE.equals(a.getEstado()) && a.getFechacreacion() != null) {
            Date vence = new Date(a.getFechacreacion().getTime() + RETENCION_MIN * 60000L);
            v.setVenceEn(vence);
            v.setMinutosParaVencer((int) Math.max(0, (vence.getTime() - ahora.getTime() + 59999L) / 60000L));
        }
        List<Detalleapartado> detalles = new ArrayList<>(a.getDetalleapartadoList());
        Collections.sort(detalles, (x, y) -> Integer.compare(x.getId() == null ? 0 : x.getId(), y.getId() == null ? 0 : y.getId()));
        for (Detalleapartado d : detalles) {
            v.getLineas().add(new ComandaVista.Linea(d.getPlatilloid().getId(), d.getPlatilloid().getNombre(),
                    d.getCantidad(), d.getPreciounitario(), null));
        }
        if (a.getPagoList() != null) {
            for (Pago p : a.getPagoList()) {
                if (!Constantes.PAGO_CONFIRMADO.equals(p.getEstado())) {
                    continue;
                }
                if (Constantes.PAGO_SALDO.equals(p.getConcepto())) {
                    v.setMetodoSaldo(p.getMetodo());
                    v.setReferenciaSaldo(p.getReferencia());
                    v.setFechaSaldo(p.getFecha());
                } else {
                    v.setMetodoPago(p.getMetodo());
                    v.setReferenciaPago(p.getReferencia());
                    v.setFechaPago(p.getFecha());
                }
            }
        }
        return v;
    }
}
