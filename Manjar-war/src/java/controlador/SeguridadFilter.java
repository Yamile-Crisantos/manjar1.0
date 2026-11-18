package controlador;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import logicaneg.Constantes;

/**
 * Control de acceso: decide quién puede abrir cada vista.
 *
 * Reglas:
 *  - Vistas PÚBLICAS (lista de abajo): cualquiera.
 *  - Vistas con roles (mapa PERMISOS): solo esos roles.
 *  - Cualquier otra vista dentro de /vistas/: requiere haber iniciado sesión
 *    (así, una vista nueva queda protegida por defecto).
 *
 * Sin sesión          -> redirige a acceso.xhtml?destino=<vista>
 * Con sesión sin rol  -> redirige al panel con aviso.
 *
 * Además:
 *  - El personal de un negocio (dueño, gerente, mesero) no usa la página de
 *    inicio del portal: si la abre, va directo a su panel.
 *  - Quien ya inició sesión no necesita ver acceso ni registro: va al panel.
 *
 * El rol se lee de la HttpSession (lo escribe SesionBean al iniciar sesión).
 */
@WebFilter(filterName = "SeguridadFilter", urlPatterns = {"/vistas/*"})
public class SeguridadFilter implements Filter {

    private static final Set<String> PUBLICAS = new HashSet<>(Arrays.asList(
            "/vistas/inicio.xhtml",
            "/vistas/menu.xhtml",
            "/vistas/acceso.xhtml",
            "/vistas/registro.xhtml"));

    private static final Map<String, Set<Integer>> PERMISOS = new HashMap<>();

    /** Quienes trabajan en un negocio. */
    private static final Set<Integer> PERSONAL = roles(Constantes.ROL_DUENO, Constantes.ROL_GERENTE, Constantes.ROL_MESERO);

    static {
        Set<Integer> todos = roles(Constantes.ROL_CLIENTE, Constantes.ROL_DUENO, Constantes.ROL_GERENTE, Constantes.ROL_MESERO);
        PERMISOS.put("/vistas/panel.xhtml", todos);
        PERMISOS.put("/vistas/apartar.xhtml", roles(Constantes.ROL_CLIENTE));
        PERMISOS.put("/vistas/platillos.xhtml", roles(Constantes.ROL_DUENO, Constantes.ROL_GERENTE));
        PERMISOS.put("/vistas/personal.xhtml", roles(Constantes.ROL_DUENO));
        PERMISOS.put("/vistas/comandas.xhtml", roles(Constantes.ROL_DUENO, Constantes.ROL_GERENTE, Constantes.ROL_MESERO));
        PERMISOS.put("/vistas/caja.xhtml", roles(Constantes.ROL_DUENO, Constantes.ROL_GERENTE));
    }

    private static Set<Integer> roles(Integer... r) {
        return new HashSet<>(Arrays.asList(r));
    }

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        // Evita que el botón "Atrás" muestre páginas guardadas después de cerrar sesión
        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        resp.setHeader("Pragma", "no-cache");
        resp.setDateHeader("Expires", 0);

        String ruta = rutaNormalizada(req);
        HttpSession sesion = req.getSession(false);
        Integer rol = sesion == null ? null : (Integer) sesion.getAttribute(SesionBean.ATTR_ROL);

        if (rol != null && esPaginaDePortal(ruta, req, rol)) {
            redirigir(req, resp, "/vistas/panel.xhtml");
            return;
        }
        if (PUBLICAS.contains(ruta)) {
            chain.doFilter(request, response);
            return;
        }

        if (rol == null) {
            redirigir(req, resp, "/vistas/acceso.xhtml?destino=" + URLEncoder.encode(destino(req, ruta), "UTF-8"));
            return;
        }
        Set<Integer> permitidos = PERMISOS.get(ruta);
        if (permitidos != null && !permitidos.contains(rol)) {
            redirigir(req, resp, "/vistas/panel.xhtml?acceso=denegado");
            return;
        }
        chain.doFilter(request, response);
    }

    /**
     * true si alguien con sesión abre una página que no le sirve:
     * el inicio del portal (si es personal de un negocio) o acceso/registro (cualquiera).
     * Solo se redirigen las consultas GET; un envío de formulario sigue su curso.
     */
    private boolean esPaginaDePortal(String ruta, HttpServletRequest req, Integer rol) {
        if (!"GET".equalsIgnoreCase(req.getMethod())) {
            return false;
        }
        if ("/vistas/inicio.xhtml".equals(ruta)) {
            return PERSONAL.contains(rol);
        }
        return "/vistas/acceso.xhtml".equals(ruta) || "/vistas/registro.xhtml".equals(ruta);
    }

    /** Ruta dentro de la aplicación, en minúsculas y sin el prefijo /faces si existiera. */
    private String rutaNormalizada(HttpServletRequest req) {
        String ruta = req.getServletPath() + (req.getPathInfo() == null ? "" : req.getPathInfo());
        ruta = ruta.toLowerCase(Locale.ROOT);
        if (ruta.startsWith("/faces/")) {
            ruta = ruta.substring("/faces".length());
        }
        return ruta;
    }

    /** Vista a la que hay que volver después de iniciar sesión, p. ej. /vistas/apartar?e=3 */
    private String destino(HttpServletRequest req, String ruta) {
        String vista = ruta.endsWith(".xhtml") ? ruta.substring(0, ruta.length() - ".xhtml".length()) : ruta;
        String e = req.getParameter("e");
        if (e != null && e.matches("[0-9]{1,9}")) {
            return vista + "?e=" + e;
        }
        return vista;
    }

    private void redirigir(HttpServletRequest req, HttpServletResponse resp, String urlInterna) throws IOException {
        String url = req.getContextPath() + urlInterna;
        if ("partial/ajax".equals(req.getHeader("Faces-Request"))) {
            // Petición AJAX (PrimeFaces): hay que responder con XML de redirección
            resp.setContentType("text/xml;charset=UTF-8");
            resp.getWriter().write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                    + "<partial-response><redirect url=\"" + url.replace("&", "&amp;") + "\"/></partial-response>");
        } else {
            resp.sendRedirect(url);
        }
    }
}
