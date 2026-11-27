package controlador;

import java.io.Serializable;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import javax.enterprise.context.ApplicationScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;

/**
 * Fechas y horas para las pantallas, en la hora del servidor (la del negocio) y en español.
 * Se usa desde las vistas como #{fmt.fechaHora(x.fecha)}; si la fecha es null devuelve una raya.
 * (No se usa f:convertDateTime porque por omisión muestra la hora en UTC.)
 */
@Named("fmt")
@ApplicationScoped
public class FormatoBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Locale ES = new Locale("es", "MX");

    public String fechaHora(Date d) {
        return d == null ? "—" : new SimpleDateFormat("dd/MM/yyyy HH:mm", ES).format(d);
    }

    public String fecha(Date d) {
        return d == null ? "—" : new SimpleDateFormat("dd/MM/yyyy", ES).format(d);
    }

    public String hora(Date d) {
        return d == null ? "—" : new SimpleDateFormat("HH:mm", ES).format(d);
    }

    /** "dd/MM HH:mm": para listas del día donde el año sobra. */
    public String diaHora(Date d) {
        return d == null ? "—" : new SimpleDateFormat("dd/MM HH:mm", ES).format(d);
    }

    /**
     * Dirección que se pone en el src de una foto de platillo: un enlace http/https se usa tal cual; la ruta interna
     * de una foto subida (imagen/platillo/12?v=...) se completa con la dirección de esta aplicación.
     * Cualquier otra cosa devuelve vacío, así nunca llega un "javascript:" o un "data:" a la página.
     */
    public String imagen(String url) {
        if (url == null || url.trim().isEmpty()) {
            return "";
        }
        String u = url.trim();
        String bajo = u.toLowerCase(Locale.ROOT);
        if (bajo.startsWith("https://") || bajo.startsWith("http://")) {
            return u;
        }
        if (u.startsWith(logicaneg.MPlatillo.RUTA_IMAGEN)) {
            return FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath() + "/" + u;
        }
        return "";
    }

    /**
     * Nombre del icono que se dibuja en lugar de la foto cuando un platillo no la tiene. Se elige por el nombre de su
     * categoría ("Bebidas" → bebida, "Postres" → postre...); si no se reconoce, el de un plato. Las vistas lo usan
     * como clase: class="mj-ico i-#{fmt.icono(categoria)}".
     */
    public String icono(String categoria) {
        if (categoria == null) {
            return "plato";
        }
        String c = Normalizer.normalize(categoria, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
        if (tiene(c, "bebida", "refresco", "agua", "jugo", "cafe", "licuado", "cerveza", "michelada", "horchata",
                "limonada", "malteada", "chocolate", "atole", "te ")) {
            return "bebida";
        }
        if (tiene(c, "postre", "dulce", "pastel", "helado", "flan", "galleta", "gelatina", "pay", "nieve")) {
            return "postre";
        }
        if (tiene(c, "desayuno", "almuerzo", "huevo", "chilaquil", "hotcake", "molletes", "pan ")) {
            return "desayuno";
        }
        if (tiene(c, "sopa", "caldo", "consome", "pozole", "menudo", "crema", "birria")) {
            return "sopa";
        }
        if (tiene(c, "carne", "pollo", "res", "cerdo", "asado", "parrilla", "pescado", "marisco", "fuerte", "guisado",
                "comida corrida", "especialidad")) {
            return "carne";
        }
        if (tiene(c, "antojito", "taco", "tamal", "torta", "quesadilla", "gordita", "tlayuda", "memela", "empanada",
                "sope", "tostada", "enchilada", "entrada", "botana")) {
            return "antojito";
        }
        if (tiene(c, "ensalada", "verdura", "vegetar", "vegano", "fruta", "ligero")) {
            return "ensalada";
        }
        return "plato";
    }

    private static boolean tiene(String texto, String... claves) {
        for (String k : claves) {
            if (texto.contains(k)) {
                return true;
            }
        }
        return false;
    }
}
