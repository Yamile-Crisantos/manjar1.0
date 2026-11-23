package controlador;

import java.io.Serializable;
import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import logicaneg.MUsuario;
import logicaneg.ReglaNegocioException;
import modelo.Usuario;

/**
 * Registro de cliente desde el portal público (HU-01).
 * Al terminar, el cliente queda con la sesión iniciada.
 */
@Named("registroBean")
@ViewScoped
public class RegistroBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private MUsuario mUsuario;

    @Inject
    private SesionBean sesion;

    private String nombre;
    private String correo;
    private String clave;
    private String confirmacion;
    private String destino;

    public String registrar() {
        if (clave == null || !clave.equals(confirmacion)) {
            Mensajes.error("Las contraseñas no coinciden.");
            return null;
        }
        try {
            Usuario u = mUsuario.registrarCliente(nombre, correo, clave);
            sesion.iniciar(u);
            Mensajes.info("Cuenta creada. ¡Bienvenido, " + u.getNombre() + "!");
            Mensajes.conservarEnRedireccion();
            return Navegacion.despuesDeAcceso(destino);
        } catch (ReglaNegocioException e) {
            Mensajes.error(e.getMessage());
            clave = null;
            confirmacion = null;
            return null;
        }
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

    /**
     * true si el visitante venía del menú de un negocio para apartar platillos: en ese caso
     * la pantalla se muestra con la marca del negocio y sin enlaces del servicio Manjar.
     */
    public boolean isParaApartar() {
        return destino != null && destino.startsWith("/vistas/apartar");
    }

    public String getDestino() {
        return destino;
    }

    public void setDestino(String destino) {
        this.destino = Navegacion.esDestinoValido(destino) ? destino : null;
    }
}
