package controlador;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.model.SelectItem;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import logicaneg.Constantes;
import logicaneg.MUsuario;
import logicaneg.ReglaNegocioException;
import modelo.Usuario;

/**
 * Alta y administración de cuentas de personal (HU-02). Solo el Dueño.
 * El negocio siempre sale de la sesión del Dueño.
 */
@Named("personalBean")
@ViewScoped
public class PersonalBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private MUsuario mUsuario;

    @Inject
    private SesionBean sesion;

    private List<Usuario> personal;

    private String nombre;
    private String correo;
    private String clave;
    private Integer rol = Constantes.ROL_MESERO;

    @PostConstruct
    public void init() {
        cargar();
    }

    private void cargar() {
        personal = mUsuario.listarPersonal(sesion.getEstablecimientoId());
    }

    public void crear() {
        try {
            mUsuario.crearPersonal(sesion.getEstablecimientoId(), nombre, correo, clave, rol);
            Mensajes.info("Cuenta creada para " + nombre.trim() + " (" + Constantes.nombreRol(rol) + ").");
            nombre = null;
            correo = null;
            clave = null;
            rol = Constantes.ROL_MESERO;
            cargar();
        } catch (ReglaNegocioException e) {
            Mensajes.error(e.getMessage());
            clave = null;
        }
    }

    public void alternarEstado(Usuario u) {
        boolean activar = u.getEstado() == null || u.getEstado() != Constantes.ESTADO_ACTIVO;
        try {
            mUsuario.cambiarEstado(sesion.getEstablecimientoId(), u.getId(), activar);
            Mensajes.info("Cuenta de " + u.getNombre() + (activar ? " activada." : " desactivada."));
            cargar();
        } catch (ReglaNegocioException e) {
            Mensajes.error(e.getMessage());
        }
    }

    public List<SelectItem> getOpcionesRol() {
        List<SelectItem> opciones = new ArrayList<>();
        opciones.add(new SelectItem(Constantes.ROL_MESERO, "Mesero"));
        opciones.add(new SelectItem(Constantes.ROL_GERENTE, "Gerente"));
        return opciones;
    }

    public String nombreRol(Integer r) {
        return Constantes.nombreRol(r);
    }

    public List<Usuario> getPersonal() {
        return personal;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

    public Integer getRol() {
        return rol;
    }

    public void setRol(Integer rol) {
        this.rol = rol;
    }
}
