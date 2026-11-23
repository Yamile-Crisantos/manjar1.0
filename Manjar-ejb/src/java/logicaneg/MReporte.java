package logicaneg;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import modelo.Establecimiento;
import modelo.Platillo;
import modelo.Usuario;

/**
 * HU-20 (ventas y rentabilidad de una sucursal por periodo, platillo y mesero) y HU-21 (resumen de todas las
 * sucursales). Solo el Dueño: el Gerente no entra a ninguno de los dos reportes (HU-19 y HU-21).
 *
 * Qué cuenta como venta:
 *  - una comanda cobrada (con fecha de cobro) y no cancelada, en la fecha en que se cobró;
 *  - un apartado con su pago confirmado (pagado, listo o entregado), en la fecha en que se pagó.
 * El precio y el costo salen de lo que valía el platillo cuando se vendió (se guardan en cada renglón), así que
 * cambiar un precio después no altera los reportes anteriores. Si un renglón no tiene costo, se suma a los
 * ingresos pero no al costo, y el reporte avisa de qué platillos son.
 */
@Stateless
@LocalBean
public class MReporte {

    public static final int MAX_DIAS = 366;
    public static final String AGRUPAR_DIA = "DIA";
    public static final String AGRUPAR_MES = "MES";
    public static final String APARTADOS_EN_LINEA = "Apartados en línea";

    @PersistenceContext(unitName = "Manjar-ejbPU")
    private EntityManager em;

    @EJB
    private MAcceso mAcceso;

    @EJB
    private MCaja mCaja;

    /** Un renglón vendido, venga de una comanda o de un apartado. */
    private static class Linea {
        Date fecha;
        String ventaId; // "C12" (comanda) o "A7" (apartado)
        Integer meseroId;
        String meseroNombre;
        Integer platilloId;
        String platillo;
        int cantidad;
        BigDecimal precio;
        BigDecimal costo;
        boolean apartado;
    }

    // ------------------------------------------------------------------
    // Opciones de los filtros
    // ------------------------------------------------------------------
    /** Platillos de la sucursal (id, nombre) para el filtro del reporte. */
    public Map<Integer, String> platillos(Integer sucursalId, Integer duenoId) throws ReglaNegocioException {
        mAcceso.dueno(sucursalId, duenoId);
        Map<Integer, String> r = new LinkedHashMap<>();
        for (Object[] f : em.createQuery("SELECT p.id, p.nombre FROM Platillo p WHERE p.establecimientoid.id = :e "
                + "ORDER BY p.nombre", Object[].class).setParameter("e", sucursalId).getResultList()) {
            r.put((Integer) f[0], (String) f[1]);
        }
        return r;
    }

    /** Quienes han registrado comandas en la sucursal o trabajan en ella (id, nombre) para el filtro por mesero. */
    public Map<Integer, String> meseros(Integer sucursalId, Integer duenoId) throws ReglaNegocioException {
        mAcceso.dueno(sucursalId, duenoId);
        Map<Integer, String> r = new LinkedHashMap<>();
        for (Usuario u : em.createQuery(
                "SELECT DISTINCT u FROM Usuario u WHERE (u.establecimientoid.id = :e AND u.rol IN (:g, :m)) "
                + "OR u.id IN (SELECT c.meseroid.id FROM Comanda c WHERE c.establecimientoid.id = :e) "
                + "ORDER BY u.nombre", Usuario.class)
                .setParameter("e", sucursalId).setParameter("g", Constantes.ROL_GERENTE)
                .setParameter("m", Constantes.ROL_MESERO).getResultList()) {
            r.put(u.getId(), u.getNombre());
        }
        return r;
    }

    // ------------------------------------------------------------------
    // HU-20: reporte de una sucursal
    // ------------------------------------------------------------------
    public ReporteVista ventas(Integer sucursalId, Integer duenoId, Date desde, Date hasta, String agrupar,
            Integer platilloId, Integer meseroId) throws ReglaNegocioException {
        mAcceso.dueno(sucursalId, duenoId);
        Establecimiento est = mAcceso.establecimiento(sucursalId);
        Date[] rango = rango(desde, hasta);
        String modo = agrupar == null || agrupar.isEmpty() ? AGRUPAR_DIA : agrupar;
        if (!AGRUPAR_DIA.equals(modo) && !AGRUPAR_MES.equals(modo)) {
            throw new ReglaNegocioException("Elige si quieres ver las ventas por día o por mes.");
        }
        if (platilloId != null) {
            Platillo p = em.find(Platillo.class, platilloId);
            if (p == null || p.getEstablecimientoid() == null || !p.getEstablecimientoid().getId().equals(sucursalId)) {
                throw new ReglaNegocioException("Ese platillo no es de esta sucursal.");
            }
        }
        if (meseroId != null && !meseros(sucursalId, duenoId).containsKey(meseroId)) {
            throw new ReglaNegocioException("Esa persona no ha trabajado en esta sucursal.");
        }

        ReporteVista v = new ReporteVista();
        v.setSucursalId(sucursalId);
        v.setSucursalNombre(est.getNombre());
        v.setDesde(rango[0]);
        v.setHasta(MComanda.inicioDelDia(hasta));
        v.setAgrupar(modo);

        Map<String, FilaReporte> periodos = new TreeMap<>();
        Map<Integer, FilaReporte> platillos = new LinkedHashMap<>();
        Map<String, FilaReporte> meserosFilas = new LinkedHashMap<>();
        Set<String> ventasTotal = new HashSet<>();
        Map<String, Set<String>> ventasPeriodo = new HashMap<>();
        Map<Integer, Set<String>> ventasPlatillo = new HashMap<>();
        Map<String, Set<String>> ventasMesero = new HashMap<>();
        Set<String> sinCosto = new java.util.TreeSet<>();

        for (Linea l : lineas(sucursalId, rango[0], rango[1], platilloId, meseroId)) {
            String clavePeriodo = clavePeriodo(l.fecha, modo);
            FilaReporte fp = periodos.get(clavePeriodo);
            if (fp == null) {
                fp = new FilaReporte(clavePeriodo, etiquetaPeriodo(l.fecha, modo));
                periodos.put(clavePeriodo, fp);
            }
            FilaReporte fl = platillos.get(l.platilloId);
            if (fl == null) {
                fl = new FilaReporte(String.valueOf(l.platilloId), l.platillo);
                platillos.put(l.platilloId, fl);
            }
            String claveMesero = l.apartado ? "apartados" : "m" + l.meseroId;
            FilaReporte fm = meserosFilas.get(claveMesero);
            if (fm == null) {
                fm = new FilaReporte(claveMesero, l.apartado ? APARTADOS_EN_LINEA : l.meseroNombre);
                meserosFilas.put(claveMesero, fm);
            }
            fp.sumar(l.cantidad, l.precio, l.costo);
            fl.sumar(l.cantidad, l.precio, l.costo);
            fm.sumar(l.cantidad, l.precio, l.costo);
            v.getTotal().sumar(l.cantidad, l.precio, l.costo);
            if (l.costo == null) {
                sinCosto.add(l.platillo);
            }
            if (ventasTotal.add(l.ventaId)) {
                v.getTotal().contarVenta();
            }
            contar(ventasPeriodo, clavePeriodo, l.ventaId, fp);
            contar(ventasPlatillo, l.platilloId, l.ventaId, fl);
            contar(ventasMesero, claveMesero, l.ventaId, fm);
        }

        v.getPorPeriodo().addAll(periodos.values());
        List<FilaReporte> lp = new ArrayList<>(platillos.values());
        Collections.sort(lp, new Comparator<FilaReporte>() {
            @Override
            public int compare(FilaReporte a, FilaReporte b) {
                int c = b.getGanancia().compareTo(a.getGanancia());
                return c != 0 ? c : a.getEtiqueta().compareToIgnoreCase(b.getEtiqueta());
            }
        });
        v.getPorPlatillo().addAll(lp);
        List<FilaReporte> lm = new ArrayList<>(meserosFilas.values());
        Collections.sort(lm, new Comparator<FilaReporte>() {
            @Override
            public int compare(FilaReporte a, FilaReporte b) {
                int c = b.getIngresos().compareTo(a.getIngresos());
                return c != 0 ? c : a.getEtiqueta().compareToIgnoreCase(b.getEtiqueta());
            }
        });
        v.getPorMesero().addAll(lm);
        v.getPlatillosSinCosto().addAll(sinCosto);
        return v;
    }

    // ------------------------------------------------------------------
    // HU-21: vista consolidada
    // ------------------------------------------------------------------
    public ConsolidadoVista consolidado(Integer duenoId, Date desde, Date hasta) throws ReglaNegocioException {
        Usuario dueno = mAcceso.usuarioActivo(duenoId);
        if (dueno.getRol() == null || dueno.getRol() != Constantes.ROL_DUENO) {
            throw new ReglaNegocioException("Solo el dueño puede hacer esto.");
        }
        Date[] rango = rango(desde, hasta);
        ConsolidadoVista c = new ConsolidadoVista();
        c.setDesde(rango[0]);
        c.setHasta(MComanda.inicioDelDia(hasta));
        Set<String> ventasTotal = new HashSet<>();
        Date ahora = new Date();
        Date limitePendiente = new Date(ahora.getTime() - MApartado.RETENCION_MIN * 60000L);
        for (Establecimiento e : em.createQuery("SELECT e FROM Establecimiento e WHERE e.duenoid.id = :d ORDER BY e.id",
                Establecimiento.class).setParameter("d", duenoId).getResultList()) {
            ResumenSucursal r = new ResumenSucursal();
            r.setSucursalId(e.getId());
            r.setNombre(e.getNombre());
            Usuario g = e.getGerenteid();
            r.setGerente(g != null && g.getEstado() != null && g.getEstado() == Constantes.ESTADO_ACTIVO ? g.getNombre() : null);
            Set<String> ventasSucursal = new HashSet<>();
            for (Linea l : lineas(e.getId(), rango[0], rango[1], null, null)) {
                r.getVentas().sumar(l.cantidad, l.precio, l.costo);
                c.getTotal().sumar(l.cantidad, l.precio, l.costo);
                if (ventasSucursal.add(l.ventaId)) {
                    r.getVentas().contarVenta();
                }
                if (ventasTotal.add(e.getId() + l.ventaId)) {
                    c.getTotal().contarVenta();
                }
            }
            CajaVista caja = mCaja.abierta(e.getId(), duenoId);
            r.setCajaAbierta(caja != null);
            r.setEfectivoEsperado(caja == null ? null : caja.getMontoEsperado());
            r.setComandasEnCocina(contar("SELECT COUNT(c) FROM Comanda c WHERE c.establecimientoid.id = :e AND c.estado IN (:a, :b, :l)",
                    e.getId(), "a", Constantes.COMANDA_PENDIENTE, "b", Constantes.COMANDA_EN_PREPARACION, "l", Constantes.COMANDA_LISTA));
            r.setPorCobrar(contar("SELECT COUNT(c) FROM Comanda c WHERE c.establecimientoid.id = :e AND c.estado <> :x AND c.fechacobro IS NULL",
                    e.getId(), "x", Constantes.COMANDA_CANCELADA));
            r.setApartadosActivos(em.createQuery(
                    "SELECT COUNT(a) FROM Apartado a WHERE a.establecimientoid.id = :e AND (a.estado IN (:g, :l) "
                    + "OR (a.estado = :p AND a.fechacreacion >= :limite))", Long.class)
                    .setParameter("e", e.getId()).setParameter("g", Constantes.APARTADO_PAGADO)
                    .setParameter("l", Constantes.APARTADO_LISTO).setParameter("p", Constantes.APARTADO_PENDIENTE)
                    .setParameter("limite", limitePendiente).getSingleResult());
            r.setPlatillosDisponibles(contar("SELECT COUNT(p) FROM Platillo p WHERE p.establecimientoid.id = :e AND p.estado = :s",
                    e.getId(), "s", Constantes.PLATILLO_DISPONIBLE));
            r.setPlatillosAgotados(contar("SELECT COUNT(p) FROM Platillo p WHERE p.establecimientoid.id = :e AND p.estado <> :s",
                    e.getId(), "s", Constantes.PLATILLO_DISPONIBLE));
            c.getSucursales().add(r);
            c.setCajasAbiertas(c.getCajasAbiertas() + (r.isCajaAbierta() ? 1 : 0));
            c.setComandasEnCocina(c.getComandasEnCocina() + r.getComandasEnCocina());
            c.setPorCobrar(c.getPorCobrar() + r.getPorCobrar());
            c.setApartadosActivos(c.getApartadosActivos() + r.getApartadosActivos());
        }
        return c;
    }

    // ------------------------------------------------------------------
    // Consulta de renglones vendidos
    // ------------------------------------------------------------------
    private List<Linea> lineas(Integer sucursalId, Date desde, Date hastaExclusivo, Integer platilloId, Integer meseroId) {
        List<Linea> r = new ArrayList<>();
        TypedQuery<Object[]> qc = em.createQuery(
                "SELECT c.id, c.fechacobro, c.meseroid.id, c.meseroid.nombre, d.platilloid.id, d.platilloid.nombre, d.cantidad, "
                + "COALESCE(d.preciounitario, d.platilloid.precio), COALESCE(d.costounitario, d.platilloid.costo) "
                + "FROM Detallecomanda d JOIN d.comandaid c WHERE c.establecimientoid.id = :e AND c.estado <> :cancelada "
                + "AND c.fechacobro IS NOT NULL AND c.fechacobro >= :desde AND c.fechacobro < :hasta"
                + (platilloId == null ? "" : " AND d.platilloid.id = :p")
                + (meseroId == null ? "" : " AND c.meseroid.id = :m"), Object[].class)
                .setParameter("e", sucursalId).setParameter("cancelada", Constantes.COMANDA_CANCELADA)
                .setParameter("desde", desde).setParameter("hasta", hastaExclusivo);
        if (platilloId != null) {
            qc.setParameter("p", platilloId);
        }
        if (meseroId != null) {
            qc.setParameter("m", meseroId);
        }
        for (Object[] f : qc.getResultList()) {
            Linea l = new Linea();
            l.ventaId = "C" + f[0];
            l.fecha = (Date) f[1];
            l.meseroId = (Integer) f[2];
            l.meseroNombre = (String) f[3];
            l.platilloId = (Integer) f[4];
            l.platillo = (String) f[5];
            l.cantidad = ((Number) f[6]).intValue();
            l.precio = (BigDecimal) f[7];
            l.costo = (BigDecimal) f[8];
            r.add(l);
        }
        if (meseroId == null) { // los apartados los levanta el cliente en línea: no tienen mesero
            TypedQuery<Object[]> qa = em.createQuery(
                    "SELECT a.id, pg.fecha, d.platilloid.id, d.platilloid.nombre, d.cantidad, "
                    + "COALESCE(d.preciounitario, d.platilloid.precio), COALESCE(d.costounitario, d.platilloid.costo) "
                    + "FROM Detalleapartado d JOIN d.apartadoid a, Pago pg WHERE pg.apartadoid = a AND pg.estado = :conf "
                    + "AND a.establecimientoid.id = :e AND a.estado IN (:g, :l, :n) "
                    + "AND pg.fecha >= :desde AND pg.fecha < :hasta"
                    + (platilloId == null ? "" : " AND d.platilloid.id = :p"), Object[].class)
                    .setParameter("conf", Constantes.PAGO_CONFIRMADO).setParameter("e", sucursalId)
                    .setParameter("g", Constantes.APARTADO_PAGADO).setParameter("l", Constantes.APARTADO_LISTO)
                    .setParameter("n", Constantes.APARTADO_ENTREGADO)
                    .setParameter("desde", desde).setParameter("hasta", hastaExclusivo);
            if (platilloId != null) {
                qa.setParameter("p", platilloId);
            }
            for (Object[] f : qa.getResultList()) {
                Linea l = new Linea();
                l.apartado = true;
                l.ventaId = "A" + f[0];
                l.fecha = (Date) f[1];
                l.platilloId = (Integer) f[2];
                l.platillo = (String) f[3];
                l.cantidad = ((Number) f[4]).intValue();
                l.precio = (BigDecimal) f[5];
                l.costo = (BigDecimal) f[6];
                r.add(l);
            }
        }
        return r;
    }

    // ------------------------------------------------------------------
    // Apoyo
    // ------------------------------------------------------------------
    /** Valida el rango de fechas y lo devuelve como [inicio del primer día, inicio del día siguiente al último). */
    private static Date[] rango(Date desde, Date hasta) throws ReglaNegocioException {
        if (desde == null || hasta == null) {
            throw new ReglaNegocioException("Elige la fecha inicial y la fecha final del reporte.");
        }
        Date inicio = MComanda.inicioDelDia(desde);
        Date ultimo = MComanda.inicioDelDia(hasta);
        if (ultimo.before(inicio)) {
            throw new ReglaNegocioException("La fecha final no puede ser anterior a la inicial.");
        }
        Calendar c = Calendar.getInstance();
        c.setTime(inicio);
        c.add(Calendar.DAY_OF_YEAR, MAX_DIAS);
        if (ultimo.after(c.getTime())) {
            throw new ReglaNegocioException("El reporte puede abarcar hasta " + MAX_DIAS + " días. Elige un periodo más corto.");
        }
        c.setTime(ultimo);
        c.add(Calendar.DAY_OF_YEAR, 1);
        return new Date[]{inicio, c.getTime()};
    }

    private static String clavePeriodo(Date f, String modo) {
        return new SimpleDateFormat(AGRUPAR_MES.equals(modo) ? "yyyy-MM" : "yyyy-MM-dd", Locale.ROOT).format(f);
    }

    private static String etiquetaPeriodo(Date f, String modo) {
        if (AGRUPAR_MES.equals(modo)) {
            String s = new SimpleDateFormat("MMMM yyyy", new Locale("es", "MX")).format(f);
            return s.substring(0, 1).toUpperCase() + s.substring(1);
        }
        return new SimpleDateFormat("EEE dd/MM/yyyy", new Locale("es", "MX")).format(f);
    }

    private static <K> void contar(Map<K, Set<String>> mapa, K clave, String ventaId, FilaReporte fila) {
        Set<String> s = mapa.get(clave);
        if (s == null) {
            s = new HashSet<>();
            mapa.put(clave, s);
        }
        if (s.add(ventaId)) {
            fila.contarVenta();
        }
    }

    /** Cuenta con hasta tres parámetros extra (nombre, valor) además de :e. */
    private long contar(String jpql, Integer sucursalId, Object... pares) {
        TypedQuery<Long> q = em.createQuery(jpql, Long.class).setParameter("e", sucursalId);
        for (int i = 0; i < pares.length; i += 2) {
            q.setParameter((String) pares[i], pares[i + 1]);
        }
        return q.getSingleResult();
    }
}
