package controlador;

import java.util.regex.Pattern;

/**
 * Decide a dónde mandar al usuario después de iniciar sesión o registrarse.
 */
public final class Navegacion {

    /** Solo se aceptan destinos internos como /vistas/apartar o /vistas/apartar?e=3 */
    private static final Pattern DESTINO_VALIDO = Pattern.compile("^/vistas/[a-z]+(\\?e=[0-9]{1,9})?$");

    private Navegacion() {
    }

    /**
     * @param destino la vista que el usuario quería abrir (puede venir null)
     * @return resultado de navegación JSF con redirección (cambia la URL)
     */
    public static String despuesDeAcceso(String destino) {
        String vista = "/vistas/panel";
        String consulta = "";
        if (destino != null && DESTINO_VALIDO.matcher(destino).matches()
                && !destino.startsWith("/vistas/acceso") && !destino.startsWith("/vistas/registro")) {
            int i = destino.indexOf('?');
            vista = i < 0 ? destino : destino.substring(0, i);
            consulta = i < 0 ? "" : destino.substring(i + 1) + "&";
        }
        return vista + "?" + consulta + "faces-redirect=true";
    }

    /** true si el destino es seguro (evita redirecciones a sitios externos). */
    public static boolean esDestinoValido(String destino) {
        return destino != null && DESTINO_VALIDO.matcher(destino).matches();
    }
}
