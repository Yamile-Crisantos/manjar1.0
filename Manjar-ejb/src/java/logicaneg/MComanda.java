package logicaneg;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;
import modelo.Comanda;
import modelo.Detallecomanda;
import modelo.Establecimiento;
import modelo.Platillo;
import modelo.Usuario;

/**
 * Comandas digitales: HU-08 (el mesero registra el pedido), HU-09 (el mesero ve el estado) y
 * HU-10 (la cocina actualiza el estado desde la pantalla compartida).
 *
 * Ciclo de una comanda:
 *   PENDIENTE -> EN_PREPARACION -> LISTA -> ENTREGADA        (CANCELADA mientras no se haya cobrado)
 * El cobro (caja, HU-11) es aparte: una comanda se puede cobrar en cualquier momento del ciclo.
 *
 * Reglas:
 *  - Cada platillo pedido descuenta sus porciones del día en la misma transacción (HU-05): si alguno
 *    ya no alcanza, no se guarda nada.
 *  - La comanda guarda el precio y el costo del momento: si después cambia el menú, no cambian las cuentas.
 *  - Todo se comprueba contra la sucursal de la sesión; un id de otra sucursal se rechaza.
 */
@Stateless
@LocalBean
public class MComanda {

    public static final int MAX_LINEAS = 40;
    public static final int MAX_CANTIDAD = 99;
    public static final int NOTA_MAX = 120;
    public static final int MESA_MAX = 50;

    @PersistenceContext(unitName = "Manjar-ejbPU")
    private EntityManager em;

    @EJB
    private MPlatillo mPlatillo;

    @EJB
    private MAcceso mAcceso;

    // ------------------------------------------------------------------
    // HU-08: registrar la comanda
    // ------------------------------------------------------------------
    /**
     * @param tipo   MESA o LLEVAR
     * @param mesa   número o nombre de la mesa; en "para llevar" es el nombre de quien recoge (opcional)
     * @param pedido lo que se pidió
     */
    public ComandaVista crear(Integer establecimientoId, Integer usuarioId, String tipo, String mesa,
            List<LineaPedido> pedido) throws ReglaNegocioException {
        Establecimiento est = mAcceso.establecimiento(establecimientoId);
        Usuario mesero = mAcceso.personal(establecimientoId, usuarioId);

        String t = tipo == null ? "" : tipo.trim().toUpperCase();
        if (!Constantes.TIPO_MESA.equals(t) && !Constantes.TIPO_LLEVAR.equals(t)) {
            throw new ReglaNegocioException("Elige si la comanda es para una mesa o para llevar.");
        }
        List<LineaPedido> lineas = unirLineas(pedido);
        String m = limpiar(mesa);
        if (Constantes.TIPO_MESA.equals(t) && m == null) {
            throw new ReglaNegocioException("Escribe el número o nombre de la mesa.");
        }
        if (m != null && m.length() > MESA_MAX) {
            throw new ReglaNegocioException("El nombre de la mesa puede tener hasta " + MESA_MAX + " caracteres.");
        }

        // Porciones totales por platillo; se descuentan en orden de id para no cruzar bloqueos entre meseros
        Map<Integer, Integer> porPlatillo = new TreeMap<>();
        for (LineaPedido l : lineas) {
            porPlatillo.merge(l.getPlatilloId(), l.getCantidad(), Integer::sum);
        }
        Map<Integer, Platillo> platillos = new LinkedHashMap<>();
        for (Map.Entry<Integer, Integer> e : porPlatillo.entrySet()) {
            platillos.put(e.getKey(), mPlatillo.descontar(establecimientoId, e.getKey(), e.getValue()));
        }

        Comanda c = new Comanda();
        c.setEstablecimientoid(est);
        c.setMeseroid(mesero);
        c.setTipo(t);
        c.setMesa(m);
        c.setEstado(Constantes.COMANDA_PENDIENTE);
        c.setFechahora(new Date());
        List<Detallecomanda> detalles = new ArrayList<>();
        for (LineaPedido l : lineas) {
            Platillo p = platillos.get(l.getPlatilloId());
            Detallecomanda d = new Detallecomanda();
            d.setComandaid(c);
            d.setPlatilloid(p);
            d.setCantidad(l.getCantidad());
            d.setPreciounitario(p.getPrecio());
            d.setCostounitario(p.getCosto());
            d.setNota(limpiar(l.getNota()));
            detalles.add(d);
        }
        c.setDetallecomandaList(detalles);
        em.persist(c);
        em.flush();
        return vista(c, new Date());
    }

    /** Valida las líneas y junta las que piden lo mismo (mismo platillo y misma indicación). */
    private List<LineaPedido> unirLineas(List<LineaPedido> pedido) throws ReglaNegocioException {
        if (pedido == null || pedido.isEmpty()) {
            throw new ReglaNegocioException("Agrega al menos un platillo a la comanda.");
        }
        Map<String, LineaPedido> unidas = new LinkedHashMap<>();
        for (LineaPedido l : pedido) {
            if (l == null || l.getPlatilloId() == null) {
                throw new ReglaNegocioException("Hay un platillo sin elegir en la comanda.");
            }
            if (l.getCantidad() < 1 || l.getCantidad() > MAX_CANTIDAD) {
                throw new ReglaNegocioException("La cantidad de cada platillo debe estar entre 1 y " + MAX_CANTIDAD + ".");
            }
            String nota = limpiar(l.getNota());
            if (nota != null && nota.length() > NOTA_MAX) {
                throw new ReglaNegocioException("La indicación para cocina puede tener hasta " + NOTA_MAX + " caracteres.");
            }
            String clave = l.getPlatilloId() + "|" + (nota == null ? "" : nota.toLowerCase());
            LineaPedido previa = unidas.get(clave);
            if (previa == null) {
                unidas.put(clave, new LineaPedido(l.getPlatilloId(), l.getCantidad(), nota));
            } else {
                previa.setCantidad(previa.getCantidad() + l.getCantidad());
                if (previa.getCantidad() > MAX_CANTIDAD) {
                    throw new ReglaNegocioException("La cantidad de cada platillo debe estar entre 1 y " + MAX_CANTIDAD + ".");
                }
            }
        }
        if (unidas.size() > MAX_LINEAS) {
            throw new ReglaNegocioException("Una comanda puede tener hasta " + MAX_LINEAS + " líneas. Registra otra para el resto.");
        }
        return new ArrayList<>(unidas.values());
    }

    // ------------------------------------------------------------------
    // Consultas
    // ------------------------------------------------------------------
    /**
     * Comandas que siguen en juego: no canceladas y sin haberse entregado y cobrado ya.
     *
     * @param meseroId si viene, solo las de ese mesero (HU-09: el mesero ve las suyas)
     */
    public List<ComandaVista> activas(Integer establecimientoId, Integer meseroId) {
        String jpql = "SELECT c FROM Comanda c WHERE c.establecimientoid.id = :e AND c.estado <> :cancelada "
                + "AND NOT (c.estado = :entregada AND c.fechacobro IS NOT NULL)"
                + (meseroId == null ? "" : " AND c.meseroid.id = :m")
                + " ORDER BY c.fechahora, c.id";
        javax.persistence.TypedQuery<Comanda> q = em.createQuery(jpql, Comanda.class)
                .setParameter("e", establecimientoId)
                .setParameter("cancelada", Constantes.COMANDA_CANCELADA)
                .setParameter("entregada", Constantes.COMANDA_ENTREGADA);
        if (meseroId != null) {
            q.setParameter("m", meseroId);
        }
        return vistas(q.getResultList());
    }

    /** Comandas de hoy que ya terminaron (entregadas y cobradas, o canceladas). */
    public List<ComandaVista> terminadasHoy(Integer establecimientoId, Integer meseroId) {
        String jpql = "SELECT c FROM Comanda c WHERE c.establecimientoid.id = :e AND c.fechahora >= :desde "
                + "AND (c.estado = :cancelada OR (c.estado = :entregada AND c.fechacobro IS NOT NULL))"
                + (meseroId == null ? "" : " AND c.meseroid.id = :m")
                + " ORDER BY c.fechahora DESC, c.id DESC";
        javax.persistence.TypedQuery<Comanda> q = em.createQuery(jpql, Comanda.class)
                .setParameter("e", establecimientoId)
                .setParameter("desde", inicioDelDia(new Date()))
                .setParameter("cancelada", Constantes.COMANDA_CANCELADA)
                .setParameter("entregada", Constantes.COMANDA_ENTREGADA)
                .setMaxResults(30);
        if (meseroId != null) {
            q.setParameter("m", meseroId);
        }
        return vistas(q.getResultList());
    }

    /** Lo que ve la pantalla de cocina: pendientes, en preparación y listas, en orden de llegada (HU-10). */
    public List<ComandaVista> tablero(Integer establecimientoId) {
        List<Comanda> r = em.createQuery(
                "SELECT c FROM Comanda c WHERE c.establecimientoid.id = :e AND c.estado IN (:a, :b, :c) "
                + "ORDER BY c.fechahora, c.id", Comanda.class)
                .setParameter("e", establecimientoId)
                .setParameter("a", Constantes.COMANDA_PENDIENTE)
                .setParameter("b", Constantes.COMANDA_EN_PREPARACION)
                .setParameter("c", Constantes.COMANDA_LISTA)
                .getResultList();
        return vistas(r);
    }

    /** Comandas sin cobrar (para la caja): no canceladas y con fechacobro vacía. */
    public List<ComandaVista> porCobrar(Integer establecimientoId) {
        List<Comanda> r = em.createQuery(
                "SELECT c FROM Comanda c WHERE c.establecimientoid.id = :e AND c.estado <> :cancelada "
                + "AND c.fechacobro IS NULL ORDER BY c.fechahora, c.id", Comanda.class)
                .setParameter("e", establecimientoId)
                .setParameter("cancelada", Constantes.COMANDA_CANCELADA)
                .getResultList();
        return vistas(r);
    }

    public ComandaVista buscar(Integer establecimientoId, Integer comandaId) throws ReglaNegocioException {
        return vista(delNegocio(establecimientoId, comandaId, false), new Date());
    }

    // ------------------------------------------------------------------
    // HU-10: la cocina cambia el estado
    // ------------------------------------------------------------------
    /**
     * Avanza (o regresa un paso) el estado de una comanda desde la cocina.
     * Permitido: PENDIENTE -> EN_PREPARACION -> LISTA, y regresar un paso por si se presionó por error.
     */
    public ComandaVista cambiarEstadoCocina(Integer establecimientoId, Integer comandaId, String destino)
            throws ReglaNegocioException {
        Comanda c = delNegocio(establecimientoId, comandaId, true);
        String origen = c.getEstado();
        boolean valido =
                (Constantes.COMANDA_PENDIENTE.equals(origen) && Constantes.COMANDA_EN_PREPARACION.equals(destino))
                || (Constantes.COMANDA_EN_PREPARACION.equals(origen) && Constantes.COMANDA_LISTA.equals(destino))
                || (Constantes.COMANDA_EN_PREPARACION.equals(origen) && Constantes.COMANDA_PENDIENTE.equals(destino))
                || (Constantes.COMANDA_LISTA.equals(origen) && Constantes.COMANDA_EN_PREPARACION.equals(destino));
        if (!valido) {
            if (Constantes.COMANDA_CANCELADA.equals(origen)) {
                throw new ReglaNegocioException("Esa comanda se canceló.");
            }
            throw new ReglaNegocioException("Esa comanda ya no está en la cocina (ahora: "
                    + vista(c, new Date()).getEstadoTexto().toLowerCase() + ").");
        }
        c.setEstado(destino);
        em.flush();
        return vista(c, new Date());
    }

    // ------------------------------------------------------------------
    // Mesero / gerente
    // ------------------------------------------------------------------
    /** Marca la comanda como entregada en la mesa (solo si la cocina ya la tiene lista). */
    public ComandaVista entregar(Integer establecimientoId, Integer usuarioId, Integer comandaId)
            throws ReglaNegocioException {
        mAcceso.personal(establecimientoId, usuarioId);
        Comanda c = delNegocio(establecimientoId, comandaId, true);
        if (!Constantes.COMANDA_LISTA.equals(c.getEstado())) {
            throw new ReglaNegocioException("Solo se puede entregar una comanda que la cocina ya marcó como lista.");
        }
        c.setEstado(Constantes.COMANDA_ENTREGADA);
        em.flush();
        return vista(c, new Date());
    }

    /**
     * Cancela una comanda y regresa sus porciones al menú. El mesero solo cancela las suyas y mientras
     * la cocina no las haya empezado; el dueño y el gerente, cualquiera que no se haya cobrado.
     */
    public ComandaVista cancelar(Integer establecimientoId, Integer usuarioId, Integer comandaId)
            throws ReglaNegocioException {
        Usuario u = mAcceso.personal(establecimientoId, usuarioId);
        Comanda c = delNegocio(establecimientoId, comandaId, true);
        if (Constantes.COMANDA_CANCELADA.equals(c.getEstado())) {
            throw new ReglaNegocioException("Esa comanda ya estaba cancelada.");
        }
        if (c.getFechacobro() != null) {
            throw new ReglaNegocioException("Esa comanda ya se cobró; no se puede cancelar.");
        }
        boolean administra = u.getRol() == Constantes.ROL_DUENO || u.getRol() == Constantes.ROL_GERENTE;
        if (!administra) {
            if (c.getMeseroid() == null || !c.getMeseroid().getId().equals(u.getId())) {
                throw new ReglaNegocioException("Solo puedes cancelar tus propias comandas.");
            }
            if (!Constantes.COMANDA_PENDIENTE.equals(c.getEstado())) {
                throw new ReglaNegocioException("La cocina ya empezó esa comanda; pide al gerente que la cancele.");
            }
        }
        Map<Integer, Integer> porPlatillo = new TreeMap<>();
        for (Detallecomanda d : c.getDetallecomandaList()) {
            porPlatillo.merge(d.getPlatilloid().getId(), d.getCantidad(), Integer::sum);
        }
        for (Map.Entry<Integer, Integer> e : porPlatillo.entrySet()) {
            mPlatillo.devolver(establecimientoId, e.getKey(), e.getValue());
        }
        c.setEstado(Constantes.COMANDA_CANCELADA);
        em.flush();
        return vista(c, new Date());
    }

    // ------------------------------------------------------------------
    // Apoyo
    // ------------------------------------------------------------------
    /** Busca la comanda y comprueba que sea de esa sucursal. Con bloqueo = true la deja bloqueada hasta terminar. */
    private Comanda delNegocio(Integer establecimientoId, Integer comandaId, boolean bloquear) throws ReglaNegocioException {
        Comanda c = null;
        if (comandaId != null) {
            c = bloquear ? em.find(Comanda.class, comandaId, LockModeType.PESSIMISTIC_WRITE)
                    : em.find(Comanda.class, comandaId);
        }
        if (c == null || c.getEstablecimientoid() == null || !c.getEstablecimientoid().getId().equals(establecimientoId)) {
            throw new ReglaNegocioException("Esa comanda no es de tu sucursal.");
        }
        return c;
    }

    private List<ComandaVista> vistas(List<Comanda> comandas) {
        Date ahora = new Date();
        List<ComandaVista> r = new ArrayList<>();
        for (Comanda c : comandas) {
            r.add(vista(c, ahora));
        }
        return r;
    }

    private ComandaVista vista(Comanda c, Date ahora) {
        ComandaVista v = new ComandaVista();
        v.setId(c.getId());
        v.setTipo(c.getTipo() == null ? Constantes.TIPO_MESA : c.getTipo());
        v.setMesa(c.getMesa());
        v.setEstado(c.getEstado());
        v.setFechaHora(c.getFechahora());
        v.setFechaCobro(c.getFechacobro());
        if (c.getFechahora() != null) {
            v.setMinutosEspera((int) Math.max(0, (ahora.getTime() - c.getFechahora().getTime()) / 60000L));
        }
        if (c.getMeseroid() != null) {
            v.setMeseroId(c.getMeseroid().getId());
            v.setMeseroNombre(c.getMeseroid().getNombre());
        }
        List<Detallecomanda> detalles = c.getDetallecomandaList() == null
                ? Collections.<Detallecomanda>emptyList() : c.getDetallecomandaList();
        List<Detallecomanda> ordenados = new ArrayList<>(detalles);
        Collections.sort(ordenados, (a, b) -> Integer.compare(
                a.getId() == null ? 0 : a.getId(), b.getId() == null ? 0 : b.getId()));
        for (Detallecomanda d : ordenados) {
            BigDecimal precio = d.getPreciounitario() != null ? d.getPreciounitario() : d.getPlatilloid().getPrecio();
            v.getLineas().add(new ComandaVista.Linea(d.getPlatilloid().getId(), d.getPlatilloid().getNombre(),
                    d.getCantidad(), precio, d.getNota()));
        }
        return v;
    }

    static Date inicioDelDia(Date d) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    /** Texto sin espacios repetidos ni caracteres de control; null si queda vacío. */
    static String limpiar(String s) {
        if (s == null) {
            return null;
        }
        String t = s.replaceAll("[\\p{Cntrl}]", " ").replaceAll("\\s+", " ").trim();
        return t.isEmpty() ? null : t;
    }
}
