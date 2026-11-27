package controlador;

import java.io.Serializable;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import logicaneg.MEstablecimiento;
import logicaneg.MPlatillo;
import logicaneg.ReglaNegocioException;
import modelo.Categoria;
import modelo.Platillo;

/**
 * Administración del menú (HU-04) y de la disponibilidad (HU-05).
 * Solo Dueño y Gerente llegan aquí (lo controla SeguridadFilter).
 *
 * IMPORTANTE: el id del negocio SIEMPRE sale de la sesión (sesion.establecimientoId),
 * nunca de un parámetro de la URL. Así un gerente no puede tocar otro negocio.
 */
@Named("platillosBean")
@ViewScoped
public class PlatillosBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private MPlatillo mPlatillo;

    @EJB
    private MEstablecimiento mEstablecimiento;

    @Inject
    private SesionBean sesion;

    private List<Platillo> platillos;
    private List<Categoria> categorias;

    /** Platillo que se está capturando/editando en el diálogo. */
    private Platillo platillo = new Platillo();
    private Integer categoriaId;
    private boolean disponible = true;

    private String nuevaCategoria;

    /** Foto nueva que eligió el dueño ("data:image/jpeg;base64,..."): la reduce el navegador antes de enviarla. */
    private String imagenArchivo;
    /** true si el dueño pidió quitar la foto. */
    private boolean quitarImagen;
    /** La foto que ya tiene el platillo que se edita (para mostrarla); vacío si no tiene. */
    private String imagenActual;
    /** true si la foto actual es un enlace (se muestra en el campo del enlace); false si es una foto subida. */
    private boolean imagenActualEsEnlace;

    @PostConstruct
    public void init() {
        cargar();
    }

    private void cargar() {
        try {
            platillos = mPlatillo.listar(sesion.getEstablecimientoId(), sesion.getUsuarioId());
            categorias = mPlatillo.listarCategorias(sesion.getEstablecimientoId(), sesion.getUsuarioId());
        } catch (ReglaNegocioException e) {
            platillos = new java.util.ArrayList<>();
            categorias = new java.util.ArrayList<>();
            Mensajes.error(e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // Diálogo de captura
    // ------------------------------------------------------------------
    public void nuevo() {
        platillo = new Platillo();
        platillo.setCantidadEstimada(0);
        platillo.setCantidadRestante(0);
        categoriaId = categorias.isEmpty() ? null : categorias.get(0).getId();
        disponible = true;
        reiniciarImagen(null);
    }

    public void editar(Platillo seleccionado) {
        // Trabajamos con una copia: si el usuario cancela, la tabla no cambia
        Platillo copia = new Platillo();
        copia.setId(seleccionado.getId());
        copia.setNombre(seleccionado.getNombre());
        copia.setDescripcion(seleccionado.getDescripcion());
        copia.setPrecio(seleccionado.getPrecio());
        copia.setCosto(seleccionado.getCosto());
        copia.setCantidadEstimada(seleccionado.getCantidadEstimada());
        copia.setCantidadRestante(seleccionado.getCantidadRestante());
        platillo = copia;
        categoriaId = seleccionado.getCategoriaid() == null ? null : seleccionado.getCategoriaid().getId();
        disponible = seleccionado.getEstado() != null && seleccionado.getEstado() == 1;
        reiniciarImagen(seleccionado.getImagenurl());
        // Un enlace se muestra en su campo; una foto subida solo se ve en la vista previa
        copia.setImagenurl(imagenActualEsEnlace ? seleccionado.getImagenurl() : null);
    }

    private void reiniciarImagen(String actual) {
        imagenArchivo = null;
        quitarImagen = false;
        imagenActual = actual == null || actual.trim().isEmpty() ? null : actual;
        imagenActualEsEnlace = imagenActual != null && imagenActual.toLowerCase().startsWith("http");
    }

    public void guardar() {
        try {
            boolean eraNuevo = platillo.getId() == null;
            boolean sinEnlace = platillo.getImagenurl() == null || platillo.getImagenurl().trim().isEmpty();
            boolean sinArchivo = imagenArchivo == null || imagenArchivo.trim().isEmpty();
            // Si tenía un enlace y lo dejó vacío (sin subir otra foto), es que quiere quitar la foto
            boolean quitar = quitarImagen || (imagenActualEsEnlace && sinEnlace && sinArchivo);
            mPlatillo.guardar(sesion.getEstablecimientoId(), sesion.getUsuarioId(), categoriaId, platillo, disponible,
                    imagenArchivo, quitar);
            reiniciarImagen(null);
            Mensajes.info(eraNuevo ? "Platillo agregado al menú." : "Platillo actualizado.");
            cargar();
        } catch (ReglaNegocioException e) {
            Mensajes.error(e.getMessage());
            Mensajes.marcarFallo(); // el diálogo se queda abierto
        }
    }

    // ------------------------------------------------------------------
    // Acciones de la tabla
    // ------------------------------------------------------------------
    public void alternar(Platillo p) {
        ejecutar(() -> {
            Platillo r = mPlatillo.alternarDisponibilidad(sesion.getEstablecimientoId(), sesion.getUsuarioId(), p.getId());
            Mensajes.info(r.getNombre() + (r.getEstado() == 1 ? ": disponible." : ": marcado como agotado."));
        });
    }

    public void eliminar(Platillo p) {
        ejecutar(() -> {
            mPlatillo.eliminar(sesion.getEstablecimientoId(), sesion.getUsuarioId(), p.getId());
            Mensajes.info("Platillo eliminado.");
        });
    }

    public void reiniciarDia() {
        ejecutar(() -> {
            int n = mPlatillo.reiniciarDia(sesion.getEstablecimientoId(), sesion.getUsuarioId());
            Mensajes.info(n == 0
                    ? "Ningún platillo lleva control de porciones."
                    : "Día reiniciado: " + n + " platillo(s) con porciones completas.");
        });
    }

    public void crearCategoria() {
        ejecutar(() -> {
            mPlatillo.crearCategoria(sesion.getEstablecimientoId(), sesion.getUsuarioId(), nuevaCategoria);
            Mensajes.info("Categoría \"" + nuevaCategoria.trim() + "\" creada.");
            nuevaCategoria = null;
        });
    }

    /** Una acción que puede lanzar ReglaNegocioException. */
    private interface Accion {

        void run() throws ReglaNegocioException;
    }

    private void ejecutar(Accion accion) {
        try {
            accion.run();
            cargar();
        } catch (ReglaNegocioException e) {
            Mensajes.error(e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // Para la vista
    // ------------------------------------------------------------------
    /** Enlace público del menú: es el que se convierte en código QR (HU-06). */
    public String getEnlaceMenu() {
        if (sesion.getEstablecimientoId() == null) {
            return "";
        }
        String codigo = mEstablecimiento.codigoQR(sesion.getEstablecimientoId());
        return codigo == null ? "" : Enlaces.menuCompleto(codigo);
    }

    public List<Platillo> getPlatillos() {
        return platillos;
    }

    public List<Categoria> getCategorias() {
        return categorias;
    }

    public Platillo getPlatillo() {
        return platillo;
    }

    public void setPlatillo(Platillo platillo) {
        this.platillo = platillo;
    }

    public Integer getCategoriaId() {
        return categoriaId;
    }

    public void setCategoriaId(Integer categoriaId) {
        this.categoriaId = categoriaId;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }

    public String getImagenArchivo() {
        return imagenArchivo;
    }

    public void setImagenArchivo(String imagenArchivo) {
        this.imagenArchivo = imagenArchivo;
    }

    public boolean isQuitarImagen() {
        return quitarImagen;
    }

    public void setQuitarImagen(boolean quitarImagen) {
        this.quitarImagen = quitarImagen;
    }

    /** Foto que ya tiene el platillo en edición (enlace o ruta interna); null si no tiene. */
    public String getImagenActual() {
        return imagenActual;
    }

    public String getNuevaCategoria() {
        return nuevaCategoria;
    }

    public void setNuevaCategoria(String nuevaCategoria) {
        this.nuevaCategoria = nuevaCategoria;
    }
}
