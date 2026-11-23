package controlador;

import java.io.Serializable;
import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import logicaneg.MEstablecimiento;
import logicaneg.ReglaNegocioException;
import modelo.Usuario;

/**
 * "Registra tu negocio": un negocio nuevo contrata Manjar desde la página principal.
 * Se crea la cuenta del dueño y su negocio, y el dueño entra directo a su panel.
 */
@Named("registroNegocioBean")
@ViewScoped
public class RegistroNegocioBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private MEstablecimiento mEstablecimiento;

    @Inject
    private SesionBean sesion;

    private String negocio;
    private String direccion;
    private String nombre;
    private String correo;
    private String clave;
    private String confirmacion;

    public String registrar() {
        if (clave == null || !clave.equals(confirmacion)) {
            Mensajes.error("Las contraseñas no coinciden.");
            return null;
        }
        try {
            Usuario dueno = mEstablecimiento.registrarNegocio(negocio, direccion, nombre, correo, clave);
            sesion.iniciar(dueno);
            Mensajes.info("Tu negocio «" + negocio.trim() + "» ya está en Manjar. Empieza personalizando tu menú.");
            Mensajes.conservarEnRedireccion();
            return "/vistas/panel?faces-redirect=true";
        } catch (ReglaNegocioException e) {
            Mensajes.error(e.getMessage());
            clave = null;
            confirmacion = null;
            return null;
        }
    }

    public String getNegocio() {
        return negocio;
    }

    public void setNegocio(String negocio) {
        this.negocio = negocio;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
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

    public String getConfirmacion() {
        return confirmacion;
    }

    public void setConfirmacion(String confirmacion) {
        this.confirmacion = confirmacion;
    }
}
