package controlador;

import java.util.ArrayList;
import java.util.List;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import logicaneg.Contacto;
import logicaneg.MEstablecimiento;
import logicaneg.MPlatillo;
import logicaneg.Temas;
import modelo.Establecimiento;
import modelo.Platillo;

/**
 * Menú público de un negocio (HU-06: se llega con el código QR del negocio;
 * HU-07: los platillos agotados se ven marcados).
 *
 * Quién ve qué:
 *  - Visitantes y clientes: abren el menú con el código del QR del negocio,
 *    /vistas/menu.xhtml?q=<código>. No existe una lista de negocios: cada menú
 *    se alcanza solo con su QR o su enlace.
 *  - Personal (dueño, gerente, mesero): siempre ven el menú de SU negocio,
 *    aunque cambien el código en la URL. Nunca ven los menús de otros.
 *
 * El menú se muestra con el color, el estilo, el contacto y el aviso que el dueño eligió para su negocio.
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

    /** Viene del parámetro "q" de la URL (f:viewParam en menu.xhtml y apartar.xhtml). */
    private String codigo;
    /** true si la URL traía el parámetro q (aunque no tenga el formato correcto). */
    private boolean codigoIndicado;

    private Establecimiento establecimiento;
    private boolean establecimientoCargado;
    private Establecimiento marca;
    private boolean marcaCargada;
    private List<Grupo> grupos;

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
    // Negocio que se muestra
    // ------------------------------------------------------------------
    /**
     * Se ejecuta antes de dibujar la página (f:viewAction): así el negocio ya está
     * recordado en la sesión cuando se dibuja la barra de navegación.
     */
    public void recordar() {
        getEstablecimiento();
    }

    public Establecimiento getEstablecimiento() {
        if (!establecimientoCargado) {
            if (sesion.isPersonal()) {
                establecimiento = mEstablecimiento.buscar(sesion.getEstablecimientoId());
            } else {
                establecimiento = mEstablecimiento.buscarPorCodigoQR(codigo);
                if (establecimiento != null) {
                    sesion.recordarMenu(establecimiento.getCodigoQR(), establecimiento.getNombre());
                }
            }
            establecimientoCargado = true;
        }
        return establecimiento;
    }

    /** true si llegaron con un código que no corresponde a ningún negocio. */
    public boolean isCodigoNoEncontrado() {
        return !sesion.isPersonal() && codigoIndicado && getEstablecimiento() == null;
    }

    /**
     * Código del menú al que hay que volver: el de este negocio o, si no se
     * está viendo uno, el último que se consultó en la sesión.
     */
    public String getCodigoVigente() {
        Establecimiento e = getEstablecimiento();
        if (e != null && e.getCodigoQR() != null) {
            return e.getCodigoQR();
        }
        return sesion.getUltimoMenuCodigo();
    }

    /**
     * El negocio cuya marca se usa para vestir la página (encabezado, colores y estilo): el del menú que
     * se está viendo o, en apartar / iniciar sesión / crear cuenta, el último menú que el visitante consultó.
     * null si no hay ninguno (entonces la página se ve con el aspecto general de Manjar).
     */
    public Establecimiento getMarca() {
        Establecimiento e = getEstablecimiento();
        if (e != null || sesion.isPersonal() || codigoIndicado) {
            return e;
        }
        if (!marcaCargada) {
            marca = mEstablecimiento.buscarPorCodigoQR(sesion.getUltimoMenuCodigo());
            marcaCargada = true;
        }
        return marca;
    }

    // ------------------------------------------------------------------
    // Personalización del menú (lo que eligió el dueño)
    // ------------------------------------------------------------------
    public String getColorAcento() {
        Establecimiento e = getMarca();
        return Temas.normalizar(e == null ? null : e.getColorMenu());
    }

    /**
     * Variables de color del menú, para el atributo style de la página.
     * Solo contiene colores calculados de la paleta de Temas (nunca texto escrito por el usuario).
     */
    public String getEstiloTema() {
        Establecimiento e = getMarca();
        return Temas.estilo(getColorAcento(), e == null ? null : e.getFondo());
    }

    /** Clases CSS del estilo elegido (diseño, tipografía, esquinas y fondo): solo salen de listas cerradas. */
    public String getClases() {
        Establecimiento e = getMarca();
        return e == null ? "" : Temas.clases(e.getDiseno(), e.getTipografia(), e.getEsquinas(), e.getFondo());
    }

    // ------------------------------------------------------------------
    // Contacto, horario y aviso (se muestran solo si el dueño los llenó)
    // ------------------------------------------------------------------
    public String getEnlaceTelefono() {
        Establecimiento e = getEstablecimiento();
        return e == null ? null : Contacto.enlaceTelefono(e.getTelefono());
    }

    public String getEnlaceWhatsapp() {
        Establecimiento e = getEstablecimiento();
        return e == null ? null : Contacto.enlaceWhatsapp(e.getWhatsapp());
    }

    public String getEnlaceInstagram() {
        Establecimiento e = getEstablecimiento();
        return e == null ? null : Contacto.enlaceInstagram(e.getInstagram());
    }

    public String getEnlaceFacebook() {
        Establecimiento e = getEstablecimiento();
        return e == null ? null : Contacto.enlaceFacebook(e.getFacebook());
    }

    /** true si hay algo de contacto u horario que mostrar. */
    public boolean isTieneContacto() {
        Establecimiento e = getEstablecimiento();
        return e != null && (getEnlaceTelefono() != null || getEnlaceWhatsapp() != null
                || getEnlaceInstagram() != null || getEnlaceFacebook() != null
                || (e.getHorario() != null && !e.getHorario().trim().isEmpty()));
    }

    // ------------------------------------------------------------------
    // Platillos
    // ------------------------------------------------------------------
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
    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigoIndicado = codigo != null && !codigo.isEmpty();
        // Solo se acepta un código con el formato correcto; cualquier otra cosa se ignora
        this.codigo = MEstablecimiento.codigoConFormato(codigo) ? codigo : null;
        // El código puede llegar después de que alguna expresión de la página ya consultó el negocio
        // (en un envío de formulario, JSF aplica el parámetro más tarde): se vuelve a buscar con el código nuevo.
        this.establecimientoCargado = false;
        this.establecimiento = null;
        this.marcaCargada = false;
        this.marca = null;
        this.grupos = null;
    }
}
