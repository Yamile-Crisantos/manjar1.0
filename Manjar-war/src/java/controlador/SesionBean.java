package controlador;

import java.io.Serializable;
import java.util.Map;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import logicaneg.Constantes;
import logicaneg.MEstablecimiento;
import modelo.Usuario;

/**
 * Quién está usando el sistema en esta sesión del navegador.
 *
 * En las vistas se usa como #{sesion....}, por ejemplo:
 *   rendered="#{sesion.dueno}"      muestra algo solo al Dueño
 *   #{sesion.nombre}                nombre de quien inició sesión
 *
 * Solo guardamos datos simples (no la entidad Usuario) para no arrastrar
 * relaciones de JPA dentro de la sesión.
 */
@Named("sesion")
@SessionScoped
public class SesionBean implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Claves en la HttpSession que lee SeguridadFilter (el filtro no usa CDI). */
    public static final String ATTR_ROL = "manjar.rol";
    public static final String ATTR_USUARIO = "manjar.usuarioId";
    public static final String ATTR_ESTABLECIMIENTO = "manjar.establecimientoId";

    private Integer usuarioId;
    private String nombre;
    private String correo;
    private Integer rol;
    private Integer establecimientoId;
    private String establecimientoNombre;

    /** Último menú público que vio este visitante o cliente (para volver a él desde "Apartados"). */
    private String ultimoMenuCodigo;
    private String ultimoMenuNombre;

    // ------------------------------------------------------------------
    // Acciones
    // ------------------------------------------------------------------
    /** Guarda en la sesión los datos del usuario que acaba de autenticarse. */
    public void iniciar(Usuario u) {
        this.usuarioId = u.getId();
        this.nombre = u.getNombre();
        this.correo = u.getCorreo();
        this.rol = u.getRol();
        if (u.getEstablecimientoid() != null) {
            this.establecimientoId = u.getEstablecimientoid().getId();
            this.establecimientoNombre = u.getEstablecimientoid().getNombre();
        } else {
            this.establecimientoId = null;
            this.establecimientoNombre = null;
        }
        Map<String, Object> sesionHttp = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
        sesionHttp.put(ATTR_USUARIO, usuarioId);
        sesionHttp.put(ATTR_ROL, rol);
        if (establecimientoId != null) {
            sesionHttp.put(ATTR_ESTABLECIMIENTO, establecimientoId);
        } else {
            sesionHttp.remove(ATTR_ESTABLECIMIENTO);
        }
        // Mejora pendiente (seguridad): renovar el id de sesión al iniciar sesión.
    }

    /** El dueño cambió el nombre de su negocio: se refleja en el encabezado sin volver a iniciar sesión. */
    public void actualizarNombreNegocio(String nuevoNombre) {
        if (establecimientoId != null) {
            this.establecimientoNombre = nuevoNombre;
        }
    }

    /**
     * El dueño pasa a operar otra de sus sucursales (HU-18). Quien llama ya comprobó con MSucursal.operar que
     * esa sucursal es suya; aquí solo se actualiza lo que la sesión recuerda.
     */
    public void cambiarSucursal(Integer sucursalId, String sucursalNombre) {
        if (!isDueno() || sucursalId == null) {
            return;
        }
        this.establecimientoId = sucursalId;
        this.establecimientoNombre = sucursalNombre;
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ATTR_ESTABLECIMIENTO, sucursalId);
    }

    /** Recuerda el último menú que se consultó con un código de QR. */
    public void recordarMenu(String codigo, String nombre) {
        this.ultimoMenuCodigo = codigo;
        this.ultimoMenuNombre = nombre;
    }

    /** Cierra la sesión y regresa al inicio. */
    public String cerrar() {
        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
        ec.invalidateSession();
        return "/vistas/inicio?faces-redirect=true";
    }

    /**
     * Cierra la sesión del comensal y lo regresa al menú del negocio que estaba viendo
     * (solo si el código tiene el formato correcto; si no, al inicio).
     */
    public String cerrarAlMenu(String codigo) {
        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
        ec.invalidateSession();
        if (MEstablecimiento.codigoConFormato(codigo)) {
            return "/vistas/menu?faces-redirect=true&q=" + codigo;
        }
        return "/vistas/inicio?faces-redirect=true";
    }

    // ------------------------------------------------------------------
    // Consultas para las vistas
    // ------------------------------------------------------------------
    public boolean isAutenticado() {
        return usuarioId != null;
    }

    public boolean isCliente() {
        return rolEs(Constantes.ROL_CLIENTE);
    }

    public boolean isDueno() {
        return rolEs(Constantes.ROL_DUENO);
    }

    public boolean isGerente() {
        return rolEs(Constantes.ROL_GERENTE);
    }

    public boolean isMesero() {
        return rolEs(Constantes.ROL_MESERO);
    }

    /** Dueño o Gerente: pueden administrar menú y caja de su negocio. */
    public boolean isAdministra() {
        return isDueno() || isGerente();
    }

    /** Cualquiera que trabaje en un negocio (Dueño, Gerente o Mesero). */
    public boolean isPersonal() {
        return isDueno() || isGerente() || isMesero();
    }

    public String getRolNombre() {
        return Constantes.nombreRol(rol);
    }

    public String getInicial() {
        return (nombre == null || nombre.trim().isEmpty()) ? "?" : nombre.trim().substring(0, 1).toUpperCase();
    }

    private boolean rolEs(int esperado) {
        return rol != null && rol == esperado;
    }

    // ------------------------------------------------------------------
    // Getters
    // ------------------------------------------------------------------
    public Integer getUsuarioId() {
        return usuarioId;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public Integer getRol() {
        return rol;
    }

    public Integer getEstablecimientoId() {
        return establecimientoId;
    }

    public String getEstablecimientoNombre() {
        return establecimientoNombre;
    }

    public String getUltimoMenuCodigo() {
        return ultimoMenuCodigo;
    }

    public String getUltimoMenuNombre() {
        return ultimoMenuNombre;
    }

    /** true si ya se consultó el menú de algún negocio en esta sesión. */
    public boolean isMenuReciente() {
        return ultimoMenuCodigo != null && !ultimoMenuCodigo.isEmpty();
    }
}
