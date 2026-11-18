package controlador;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import logicaneg.MEstablecimiento;
import logicaneg.MPlatillo;
import modelo.Establecimiento;
import modelo.Platillo;

/**
 * Menú público de un negocio (HU-06: se llega con el QR; HU-07: los agotados
 * se ven marcados) y lista de negocios para la página de inicio.
 *
 * Quién ve qué:
 *  - Visitantes y clientes: eligen el negocio con la URL  /vistas/menu.xhtml?e=1
 *    (o con los botones "Ver menú" de la lista).
 *  - Personal (dueño, gerente, mesero): siempre ven el menú de SU negocio,
 *    aunque cambien el número "e" en la URL. Nunca ven los menús de otros.
 */
@Named("menuPublicoBean")
@RequestScoped
public class MenuPublicoBean {

    /** A partir de cuántas porciones restantes se avisa "últimas porciones". */
    private static final int ULTIMAS_PORCIONES = 5;

    @EJB
    private MEstablecimiento mEstablecimiento;

    @EJB
    private MPlatillo mPlatillo;

    @Inject
    private SesionBean sesion;

    /** Viene del parámetro "e" de la URL (f:viewParam en menu.xhtml). */
    private Integer establecimientoId;

    private Establecimiento establecimiento;
    private boolean establecimientoCargado;
    private List<Grupo> grupos;
    private List<Establecimiento> negocios;
    private final Map<Integer, long[]> resumenes = new HashMap<>();

    /** Una categoría con sus platillos, para mostrar el menú agrupado. */
    public static class Grupo {

        private final String categoria;
        private final String ancla;
        private final List<Platillo> platillos = new ArrayList<>();

        public Grupo(String categoria, int numero) {
            this.categoria = categoria;
            this.ancla = "cat-" + numero;
        }

        public String getCategoria() {
            return categoria;
        }

        /** Identificador para el enlace interno de la página (#cat-1). */
        public String getAncla() {
            return ancla;
        }

        public List<Platillo> getPlatillos() {
            return platillos;
        }
    }

    // ------------------------------------------------------------------
    // Menú de un negocio
    // ------------------------------------------------------------------
    public Establecimiento getEstablecimiento() {
        if (!establecimientoCargado) {
            Integer id = sesion.isPersonal() ? sesion.getEstablecimientoId() : establecimientoId;
            establecimiento = mEstablecimiento.buscar(id);
            establecimientoCargado = true;
        }
        return establecimiento;
    }

    public List<Grupo> getGrupos() {
        if (grupos == null) {
            grupos = new ArrayList<>();
            Establecimiento est = getEstablecimiento();
            if (est != null) {
                Grupo actual = null;
                // mPlatillo.listar ya viene ordenado por categoría y nombre
                for (Platillo p : mPlatillo.listar(est.getId())) {
                    String cat = p.getCategoriaid() == null ? "Otros" : p.getCategoriaid().getNombre();
                    if (actual == null || !actual.getCategoria().equals(cat)) {
                        actual = new Grupo(cat, grupos.size() + 1);
                        grupos.add(actual);
                    }
                    actual.getPlatillos().add(p);
                }
            }
        }
        return grupos;
    }

    /** Platillos del negocio que se muestra. */
    public int getTotalPlatillos() {
        int total = 0;
        for (Grupo g : getGrupos()) {
            total += g.getPlatillos().size();
        }
        return total;
    }

    public int getPlatillosDisponibles() {
        int total = 0;
        for (Grupo g : getGrupos()) {
            for (Platillo p : g.getPlatillos()) {
                if (disponible(p)) {
                    total++;
                }
            }
        }
        return total;
    }

    public int getPlatillosAgotados() {
        return getTotalPlatillos() - getPlatillosDisponibles();
    }

    /** true si el platillo está disponible y le quedan pocas porciones del día. */
    public boolean ultimasPorciones(Platillo p) {
        return disponible(p)
                && p.getCantidadEstimada() != null && p.getCantidadEstimada() > 0
                && p.getCantidadRestante() != null && p.getCantidadRestante() <= ULTIMAS_PORCIONES;
    }

    private boolean disponible(Platillo p) {
        return p.getEstado() != null && p.getEstado() == 1;
    }

    // ------------------------------------------------------------------
    // Lista de negocios (inicio y menú sin elegir)
    // ------------------------------------------------------------------
    public List<Establecimiento> getNegocios() {
        if (negocios == null) {
            negocios = mEstablecimiento.listar();
        }
        return negocios;
    }

    /** Primera letra del nombre, para el círculo de cada tarjeta. */
    public String inicial(Establecimiento n) {
        String nombre = n == null ? null : n.getNombre();
        return (nombre == null || nombre.trim().isEmpty()) ? "?" : nombre.trim().substring(0, 1).toUpperCase();
    }

    public long totalDe(Establecimiento n) {
        return resumen(n)[0];
    }

    public long disponiblesDe(Establecimiento n) {
        return resumen(n)[1];
    }

    private long[] resumen(Establecimiento n) {
        long[] r = resumenes.get(n.getId());
        if (r == null) {
            r = new long[]{mPlatillo.contar(n.getId()), mPlatillo.contarDisponibles(n.getId())};
            resumenes.put(n.getId(), r);
        }
        return r;
    }

    // ------------------------------------------------------------------
    public Integer getEstablecimientoId() {
        return establecimientoId;
    }

    public void setEstablecimientoId(Integer establecimientoId) {
        this.establecimientoId = establecimientoId;
    }
}
