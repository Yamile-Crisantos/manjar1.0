package controlador;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

/**
 * Arma las direcciones públicas del menú de un negocio:
 *   /Manjar-war/vistas/menu.xhtml?q=<código del QR>
 */
public final class Enlaces {

    private Enlaces() {
    }

    /** Ruta del menú dentro de este servidor, p. ej. /Manjar-war/vistas/menu.xhtml?q=fonda-lupe-7k3m2q */
    public static String rutaMenu(String codigo) {
        return contexto() + "/vistas/menu.xhtml?q=" + codigo;
    }

    /** Dirección completa del menú (con http://servidor:puerto), la que lleva el código QR. */
    public static String menuCompleto(String codigo) {
        HttpServletRequest req = peticion();
        int puerto = req.getServerPort();
        boolean estandar = ("http".equals(req.getScheme()) && puerto == 80)
                || ("https".equals(req.getScheme()) && puerto == 443);
        return req.getScheme() + "://" + req.getServerName() + (estandar ? "" : ":" + puerto) + rutaMenu(codigo);
    }

    private static String contexto() {
        return peticion().getContextPath();
    }

    private static HttpServletRequest peticion() {
        return (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
    }
}
