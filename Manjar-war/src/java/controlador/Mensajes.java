package controlador;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

/**
 * Atajo para mostrar mensajes al usuario (aparecen en el p:growl de la plantilla).
 */
public final class Mensajes {

    private Mensajes() {
    }

    public static void info(String mensaje) {
        agregar(FacesMessage.SEVERITY_INFO, mensaje);
    }

    public static void aviso(String mensaje) {
        agregar(FacesMessage.SEVERITY_WARN, mensaje);
    }

    public static void error(String mensaje) {
        agregar(FacesMessage.SEVERITY_ERROR, mensaje);
    }

    /**
     * Llamar antes de redirigir (faces-redirect=true) si el mensaje debe verse
     * en la página de destino.
     */
    public static void conservarEnRedireccion() {
        FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
    }

    /**
     * Marca la petición como "con error de validación". PrimeFaces lo manda al
     * navegador como args.validationFailed, y así los diálogos no se cierran
     * cuando algo salió mal.
     */
    public static void marcarFallo() {
        FacesContext.getCurrentInstance().validationFailed();
    }

    private static void agregar(FacesMessage.Severity severidad, String mensaje) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severidad, mensaje, null));
    }
}
