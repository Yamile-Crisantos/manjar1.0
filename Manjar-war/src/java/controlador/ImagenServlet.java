package controlador;

import java.io.IOException;
import java.io.OutputStream;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import logicaneg.FotoPlatillo;
import logicaneg.MPlatillo;

/**
 * Sirve la foto que el dueño subió de un platillo: GET /imagen/platillo/<id>.
 *
 * El menú de un negocio es público (HU-06), así que sus fotos también. Lo que se sirve siempre es un JPEG que generó
 * el servidor al guardar la foto (nunca el archivo tal como lo mandó alguien), y la respuesta lleva
 * "nosniff" y una política que impide que se use como página. El enlace de la foto lleva "?v=" con la fecha de
 * la última foto, por eso el navegador la puede guardar un día sin que se vea una foto vieja.
 */
@WebServlet(name = "ImagenServlet", urlPatterns = {"/imagen/platillo/*"})
public class ImagenServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @EJB
    private MPlatillo mPlatillo;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer id = idDe(req.getPathInfo());
        FotoPlatillo foto = id == null ? null : mPlatillo.foto(id);
        if (foto == null || foto.getDatos() == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        String etag = "\"" + id + "-" + (foto.getFecha() == null ? 0 : foto.getFecha().getTime()) + "-" + foto.getDatos().length + "\"";
        resp.setHeader("ETag", etag);
        resp.setHeader("Cache-Control", "public, max-age=86400");
        resp.setHeader("X-Content-Type-Options", "nosniff");
        resp.setHeader("Content-Security-Policy", "default-src 'none'; style-src 'unsafe-inline'; sandbox");
        if (etag.equals(req.getHeader("If-None-Match"))) {
            resp.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            return;
        }
        resp.setContentType("image/jpeg");
        resp.setContentLength(foto.getDatos().length);
        try (OutputStream out = resp.getOutputStream()) {
            out.write(foto.getDatos());
        }
    }

    /** "/12" -> 12; cualquier otra cosa -> null. */
    private static Integer idDe(String pathInfo) {
        if (pathInfo == null || !pathInfo.matches("/\\d{1,9}")) {
            return null;
        }
        return Integer.valueOf(pathInfo.substring(1));
    }
}
