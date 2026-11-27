package logicaneg;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

/**
 * Fotos de los platillos: validación del enlace que escribe el dueño y normalización de la foto que sube.
 *
 * La foto subida NUNCA se guarda tal como llega: se lee, se reduce y se vuelve a escribir como JPEG con las
 * herramientas del servidor. Así lo que se sirve a los clientes es siempre una imagen real, sin datos ocultos,
 * sin metadatos y de un tamaño razonable, aunque quien la envió lo hiciera sin pasar por la pantalla.
 */
public final class ImagenUtil {

    /** Largo máximo del enlace (la columna es varchar(500)). */
    public static final int ENLACE_MAX = 500;
    /** Lado más largo de la foto guardada, en píxeles. */
    public static final int LADO_MAX = 1000;
    /** Tamaño máximo de la foto ya normalizada. */
    public static final int BYTES_MAX = 450 * 1024;
    /**
     * Tamaño máximo del texto que manda la pantalla (una foto en base64 que ella ya redujo, normalmente de menos de
     * 700 KB). Queda por debajo de los 2 MB que un servidor Payara acepta por petición, para que el aviso sea el de
     * Manjar y no un error del servidor.
     */
    public static final int TEXTO_MAX = 1_800_000;
    /** Píxeles máximos de la foto original: evita que una imagen enorme comprimida agote la memoria. */
    static final long PIXELES_MAX = 25_000_000L;

    private static final Pattern DATA_URL = Pattern.compile("^data:image/(jpeg|png);base64,([A-Za-z0-9+/=\\s]+)$");

    static {
        ImageIO.setUseCache(false);
    }

    private ImagenUtil() {
    }

    /**
     * Valida el enlace de una foto: solo http o https, con servidor, sin usuario ni contraseña, sin espacios y de
     * hasta {@link #ENLACE_MAX} caracteres. Devuelve el enlace limpio, o null si venía vacío.
     */
    public static String validarEnlace(String enlace) throws ReglaNegocioException {
        if (enlace == null || enlace.trim().isEmpty()) {
            return null;
        }
        String e = enlace.trim();
        if (e.length() > ENLACE_MAX) {
            throw new ReglaNegocioException("El enlace de la foto puede tener hasta " + ENLACE_MAX + " caracteres.");
        }
        if (!e.matches("^https?://[^\\s<>\"'`\\\\]+$")) {
            throw new ReglaNegocioException("El enlace de la foto debe empezar con http:// o https:// y no llevar espacios.");
        }
        try {
            URI u = new URI(e);
            if (u.getHost() == null || u.getHost().isEmpty() || !u.getHost().contains(".") && !"localhost".equalsIgnoreCase(u.getHost())) {
                throw new ReglaNegocioException("El enlace de la foto no tiene una dirección válida.");
            }
            if (u.getUserInfo() != null) {
                throw new ReglaNegocioException("El enlace de la foto no puede llevar usuario ni contraseña.");
            }
        } catch (URISyntaxException ex) {
            throw new ReglaNegocioException("El enlace de la foto no es válido.");
        }
        return e;
    }

    /**
     * Recibe la foto como "data:image/jpeg;base64,..." (así la manda la pantalla después de reducirla) y devuelve un
     * JPEG nuevo de hasta {@link #LADO_MAX} píxeles de lado y {@link #BYTES_MAX} bytes.
     */
    public static byte[] normalizarJpeg(String dataUrl) throws ReglaNegocioException {
        if (dataUrl == null || dataUrl.length() > TEXTO_MAX) {
            throw new ReglaNegocioException("La foto es demasiado grande. Elige una más pequeña (de menos de 2 MB).");
        }
        Matcher m = DATA_URL.matcher(dataUrl.trim());
        if (!m.matches()) {
            throw new ReglaNegocioException("La foto debe ser una imagen JPG o PNG.");
        }
        byte[] bruto;
        try {
            bruto = Base64.getMimeDecoder().decode(m.group(2));
        } catch (IllegalArgumentException ex) {
            throw new ReglaNegocioException("No se pudo leer la foto. Intenta con otra.");
        }
        BufferedImage origen = leer(bruto);
        BufferedImage rgb = aRgb(origen, LADO_MAX);
        float calidad = 0.85f;
        byte[] salida = escribirJpeg(rgb, calidad);
        // Si todavía pesa demasiado se baja la calidad y, de ser necesario, el tamaño
        int lado = LADO_MAX;
        while (salida.length > BYTES_MAX && (calidad > 0.5f || lado > 480)) {
            if (calidad > 0.5f) {
                calidad -= 0.1f;
            } else {
                lado = (int) (lado * 0.8);
                rgb = aRgb(origen, lado);
            }
            salida = escribirJpeg(rgb, calidad);
        }
        if (salida.length > BYTES_MAX) {
            throw new ReglaNegocioException("La foto pesa demasiado. Elige una más sencilla o más pequeña.");
        }
        return salida;
    }

    /** Lee la imagen comprobando primero sus dimensiones (sin decodificar los píxeles). */
    private static BufferedImage leer(byte[] bruto) throws ReglaNegocioException {
        try (ImageInputStream in = ImageIO.createImageInputStream(new ByteArrayInputStream(bruto))) {
            Iterator<ImageReader> lectores = ImageIO.getImageReaders(in);
            if (!lectores.hasNext()) {
                throw new ReglaNegocioException("No se pudo leer la foto. Usa una imagen JPG o PNG.");
            }
            ImageReader lector = lectores.next();
            try {
                lector.setInput(in, true, true);
                long ancho = lector.getWidth(0);
                long alto = lector.getHeight(0);
                if (ancho < 16 || alto < 16) {
                    throw new ReglaNegocioException("La foto es demasiado pequeña. Usa una de al menos 16 × 16 píxeles.");
                }
                if (ancho * alto > PIXELES_MAX) {
                    throw new ReglaNegocioException("La foto tiene demasiados píxeles. Elige una más pequeña.");
                }
                BufferedImage img = lector.read(0);
                if (img == null) {
                    throw new ReglaNegocioException("No se pudo leer la foto. Intenta con otra.");
                }
                return img;
            } finally {
                lector.dispose();
            }
        } catch (IOException | RuntimeException ex) {
            throw new ReglaNegocioException("No se pudo leer la foto. Usa una imagen JPG o PNG.");
        }
    }

    /** Reduce (nunca amplía) a un lado máximo y pinta sobre fondo blanco, porque el JPEG no tiene transparencia. */
    private static BufferedImage aRgb(BufferedImage origen, int ladoMax) {
        int w = origen.getWidth();
        int h = origen.getHeight();
        double escala = Math.min(1.0, (double) ladoMax / Math.max(w, h));
        int nw = Math.max(1, (int) Math.round(w * escala));
        int nh = Math.max(1, (int) Math.round(h * escala));
        BufferedImage rgb = new BufferedImage(nw, nh, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = rgb.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, nw, nh);
            g.drawImage(origen, 0, 0, nw, nh, null);
        } finally {
            g.dispose();
        }
        return rgb;
    }

    private static byte[] escribirJpeg(BufferedImage rgb, float calidad) throws ReglaNegocioException {
        ImageWriter escritor = ImageIO.getImageWritersByFormatName("jpeg").next();
        try (ByteArrayOutputStream salida = new ByteArrayOutputStream();
                ImageOutputStream ios = ImageIO.createImageOutputStream(salida)) {
            ImageWriteParam par = escritor.getDefaultWriteParam();
            par.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            par.setCompressionQuality(calidad);
            escritor.setOutput(ios);
            escritor.write(null, new IIOImage(rgb, null, null), par);
            ios.flush();
            return salida.toByteArray();
        } catch (IOException ex) {
            throw new ReglaNegocioException("No se pudo procesar la foto. Intenta con otra.");
        } finally {
            escritor.dispose();
        }
    }
}
