package logicaneg;

import java.util.regex.Pattern;

/**
 * Datos de contacto que un negocio muestra en su menú público: teléfono, WhatsApp,
 * Instagram y Facebook.
 *
 * Cada dato se valida y se normaliza al guardarlo, y los enlaces se arman SOLO con
 * caracteres permitidos (dígitos, letras, punto, guion). Así nunca se escribe en un
 * enlace un texto libre del usuario (por ejemplo "javascript:...").
 */
public final class Contacto {

    private static final Pattern TELEFONO = Pattern.compile("^\\+?[0-9][0-9 ()./-]{4,18}[0-9]$");
    private static final Pattern INSTAGRAM = Pattern.compile("^[A-Za-z0-9._]{1,30}$");
    private static final Pattern FACEBOOK = Pattern.compile("^[A-Za-z0-9.\\-]{5,50}$");
    private static final Pattern PREFIJO_INSTAGRAM = Pattern.compile("^(?:https?://)?(?:www\\.)?instagram\\.com/", Pattern.CASE_INSENSITIVE);
    private static final Pattern PREFIJO_FACEBOOK = Pattern.compile("^(?:https?://)?(?:www\\.|m\\.)?(?:facebook|fb)\\.com/", Pattern.CASE_INSENSITIVE);

    private Contacto() {
    }

    // ------------------------------------------------------------------
    // Validación al guardar (devuelven el valor normalizado, o null si viene vacío)
    // ------------------------------------------------------------------
    /** Teléfono: de 7 a 15 dígitos; puede llevar +, espacios, guiones y paréntesis. */
    public static String normalizarTelefono(String crudo, String nombreCampo) throws ReglaNegocioException {
        return validarTelefono(crudo, nombreCampo, 7);
    }

    /** WhatsApp: de 10 a 15 dígitos (con 10 se asume México, +52). */
    public static String normalizarWhatsapp(String crudo, String nombreCampo) throws ReglaNegocioException {
        return validarTelefono(crudo, nombreCampo, 10);
    }

    private static String validarTelefono(String crudo, String nombreCampo, int minimoDigitos) throws ReglaNegocioException {
        String t = vacioANulo(crudo);
        if (t == null) {
            return null;
        }
        t = t.replaceAll("\\s+", " ");
        int digitos = digitos(t).length();
        if (!TELEFONO.matcher(t).matches() || digitos < minimoDigitos || digitos > 15) {
            throw new ReglaNegocioException("El " + nombreCampo + " no es válido. Escribe solo números, por ejemplo 951 123 4567"
                    + (minimoDigitos > 7 ? " (con 10 dígitos o con clave de país)." : "."));
        }
        return t;
    }

    public static String normalizarInstagram(String crudo) throws ReglaNegocioException {
        String u = usuario(crudo, PREFIJO_INSTAGRAM);
        if (u == null) {
            return null;
        }
        if (!INSTAGRAM.matcher(u).matches()) {
            throw new ReglaNegocioException("Escribe solo tu usuario de Instagram, por ejemplo fondalupe.");
        }
        return u;
    }

    public static String normalizarFacebook(String crudo) throws ReglaNegocioException {
        String u = usuario(crudo, PREFIJO_FACEBOOK);
        if (u == null) {
            return null;
        }
        if (!FACEBOOK.matcher(u).matches()) {
            throw new ReglaNegocioException("Escribe solo el nombre de tu página de Facebook, por ejemplo fondalupe.oaxaca.");
        }
        return u;
    }

    /**
     * Acepta "@usuario", "usuario" o la dirección completa del perfil (en ese caso se queda con el
     * usuario y descarta lo que siga: "/", "?" o "#"). Un texto con "/" que no es una dirección del
     * sitio se deja tal cual para que la validación lo rechace.
     */
    private static String usuario(String crudo, Pattern prefijo) {
        String u = vacioANulo(crudo);
        if (u == null) {
            return null;
        }
        boolean eraDireccion = prefijo.matcher(u).find();
        u = prefijo.matcher(u).replaceFirst("");
        if (u.startsWith("@")) {
            u = u.substring(1);
        }
        if (eraDireccion) {
            int corte = u.length();
            for (char c : new char[] {'/', '?', '#'}) {
                int i = u.indexOf(c);
                if (i >= 0 && i < corte) {
                    corte = i;
                }
            }
            u = u.substring(0, corte);
        }
        u = u.trim();
        return u.isEmpty() ? null : u;
    }

    // ------------------------------------------------------------------
    // Enlaces (null si el dato no existe o no tiene el formato correcto)
    // ------------------------------------------------------------------
    public static String enlaceTelefono(String telefono) {
        String t = vacioANulo(telefono);
        if (t == null || !TELEFONO.matcher(t).matches()) {
            return null;
        }
        return "tel:" + (t.startsWith("+") ? "+" : "") + digitos(t);
    }

    public static String enlaceWhatsapp(String whatsapp) {
        String t = vacioANulo(whatsapp);
        if (t == null || !TELEFONO.matcher(t).matches()) {
            return null;
        }
        String d = digitos(t);
        if (d.length() < 10 || d.length() > 15) {
            return null;
        }
        // Sin clave de país (10 dígitos) se asume México
        return "https://wa.me/" + (d.length() == 10 ? "52" + d : d);
    }

    public static String enlaceInstagram(String usuario) {
        String u = vacioANulo(usuario);
        return u == null || !INSTAGRAM.matcher(u).matches() ? null : "https://www.instagram.com/" + u + "/";
    }

    public static String enlaceFacebook(String usuario) {
        String u = vacioANulo(usuario);
        return u == null || !FACEBOOK.matcher(u).matches() ? null : "https://www.facebook.com/" + u;
    }

    // ------------------------------------------------------------------
    static String digitos(String s) {
        return s == null ? "" : s.replaceAll("[^0-9]", "");
    }

    private static String vacioANulo(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
