package controlador;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

/**
 * Arma las direcciones públicas del menú de un negocio:
 *   /Manjar-war/vistas/menu.xhtml?q=<código del QR>
 *
 * La dirección del servidor sale, en este orden, de:
 *   1. -Dmanjar.url.publica=https://mi-dominio.com   (o la variable de entorno MANJAR_URL_PUBLICA), si el servidor
 *      se publica en Internet o en una red con un nombre fijo;
 *   2. la dirección con la que se abrió esta misma página.
 * Si el dueño abre Manjar desde su propia computadora (localhost), el QR no funcionaría en el celular de un cliente:
 * por eso la pantalla del QR avisa y ofrece las direcciones de la computadora en la red (direccionesRed).
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
        return baseServidor() + rutaMenu(codigo);
    }

    /** Dirección completa de la pantalla compartida de cocina, con su clave: http://servidor:puerto/.../cocina.xhtml?k=<clave> */
    public static String cocinaCompleto(String token) {
        return baseServidor() + contexto() + "/vistas/cocina.xhtml?k=" + token;
    }

    /**
     * Dirección pública configurada por quien instala Manjar (-Dmanjar.url.publica o MANJAR_URL_PUBLICA), sin la
     * diagonal final; cadena vacía si no hay una configurada o no es una dirección http(s) válida.
     */
    public static String urlPublicaConfigurada() {
        String v = System.getProperty("manjar.url.publica");
        if (v == null || v.trim().isEmpty()) {
            v = System.getenv("MANJAR_URL_PUBLICA");
        }
        if (v == null) {
            return "";
        }
        v = v.trim().replaceAll("/+$", "");
        return v.matches("https?://[\\x21-\\x7e]+") ? v : "";
    }

    /** Dirección del servidor: la configurada o, si no hay, la de esta misma petición (http://servidor:puerto). */
    public static String baseServidor() {
        String publica = urlPublicaConfigurada();
        if (!publica.isEmpty()) {
            return publica;
        }
        HttpServletRequest req = peticion();
        return req.getScheme() + "://" + req.getServerName() + puertoSufijo(req);
    }

    private static String puertoSufijo(HttpServletRequest req) {
        int puerto = req.getServerPort();
        boolean estandar = ("http".equals(req.getScheme()) && puerto == 80)
                || ("https".equals(req.getScheme()) && puerto == 443);
        return estandar ? "" : ":" + puerto;
    }

    /**
     * Direcciones de esta computadora dentro de su red local (p. ej. http://192.168.1.20:8080), para que el dueño
     * pueda probar el QR con un celular conectado a la misma red. Solo direcciones IPv4 privadas de interfaces activas.
     */
    public static List<String> direccionesRed() {
        List<String> lista = new ArrayList<>();
        try {
            HttpServletRequest req = peticion();
            String sufijo = puertoSufijo(req);
            for (NetworkInterface ni : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (!ni.isUp() || ni.isLoopback() || ni.isVirtual()) {
                    continue;
                }
                for (InetAddress ia : Collections.list(ni.getInetAddresses())) {
                    if (ia instanceof Inet4Address && ia.isSiteLocalAddress()) {
                        String url = req.getScheme() + "://" + ia.getHostAddress() + sufijo;
                        if (!lista.contains(url)) {
                            lista.add(url);
                        }
                    }
                }
            }
        } catch (Exception e) {
            // sin interfaces de red legibles: el dueño puede escribir la dirección a mano
        }
        return lista;
    }

    private static String contexto() {
        return peticion().getContextPath();
    }

    private static HttpServletRequest peticion() {
        return (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
    }
}
