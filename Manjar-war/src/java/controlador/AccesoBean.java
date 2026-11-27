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
 * Pantalla de inicio de sesión (HU-01 cliente, HU-03 mesero; también Dueño y Gerente).
 *
 * @ViewScoped: el bean vive mientras el usuario siga en esta pantalla, así
 * "destino" no se pierde si escribe mal la contraseña y vuelve a intentar.
 */
@Named("accesoBean")
@ViewScoped
public class AccesoBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private MUsuario mUsuario;

    @Inject
    private SesionBean sesion;

    private String correo;
    private String clave;
    /** Vista que el usuario quería abrir antes de que lo mandáramos a iniciar sesión. */
    private String destino;

    public String iniciar() {
        try {
            Usuario u = mUsuario.autenticar(correo, clave);
            sesion.iniciar(u);
            Mensajes.info("Bienvenido, " + u.getNombre() + ".");
            Mensajes.conservarEnRedireccion();
            return Navegacion.despuesDeAcceso(destino);
        } catch (ReglaNegocioException e) {
            Mensajes.error(e.getMessage());
            clave = null;
            return null; // se queda en la misma pantalla
        }
    }

    /**
     * Los accesos rápidos con las cuentas de demostración solo se muestran si el servidor se arranca con
     * -Dmanjar.demo=true (para revisar o presentar el sistema). Sin esa opción, la pantalla no los muestra nunca.
     */
    public boolean isModoDemo() {
        return Boolean.getBoolean("manjar.demo");
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
