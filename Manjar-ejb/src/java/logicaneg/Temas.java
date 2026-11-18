package logicaneg;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Cómo se ve el menú público de un negocio: color de acento y estilo (diseño de la lista,
 * tipografía, esquinas y fondo).
 *
 * Todo sale de listas cerradas, a propósito: los colores combinan con el fondo y el texto de
 * Manjar y permiten texto blanco legible en los botones. Como estos valores acaban en el CSS
 * y en las clases de la página pública, SOLO se aceptan valores de las listas (nunca un
 * texto libre del usuario).
 */
public final class Temas {

    /** Color con el que nace cada negocio (el terracota de Manjar). */
    public static final String PREDETERMINADO = "#C44A24";

    /** Un color de la paleta: nombre para mostrar y valor #RRGGBB. */
    public static final class Tema {

        private final String nombre;
        private final String hex;

        Tema(String nombre, String hex) {
            this.nombre = nombre;
            this.hex = hex;
        }

        public String getNombre() {
            return nombre;
        }

        public String getHex() {
            return hex;
        }

        public String getOscuro() {
            return Temas.oscuro(hex);
        }

        public String getSuave() {
            return Temas.suave(hex);
        }

        public String getClaro() {
            return Temas.claro(hex);
        }

        public String getProfundo() {
            return Temas.profundo(hex);
        }

        /** Color suave para fondos de un menú con fondo oscuro. */
        public String getSuaveOscuro() {
            return Temas.mezclar(hex, 0x000000, 0.72);
        }
    }

    private static final List<Tema> TODOS = Collections.unmodifiableList(Arrays.asList(
            new Tema("Terracota", PREDETERMINADO),
            new Tema("Olivo", "#4F6B3A"),
            new Tema("Petróleo", "#1F6F78"),
            new Tema("Ciruela", "#7A3355"),
            new Tema("Mostaza", "#9A6200"),
            new Tema("Chocolate", "#6B4226")));

    private Temas() {
    }

    // ------------------------------------------------------------------
    // Estilo del menú (listas cerradas)
    // ------------------------------------------------------------------
    public static final String DISENO_PREDETERMINADO = "tarjetas";
    public static final String TIPOGRAFIA_PREDETERMINADA = "clasica";
    public static final String ESQUINAS_PREDETERMINADAS = "redondas";
    public static final String FONDO_PREDETERMINADO = "claro";

    /** Una opción de estilo: valor que se guarda, nombre para mostrar y descripción corta. */
    public static final class Opcion {

        private final String valor;
        private final String nombre;
        private final String descripcion;

        Opcion(String valor, String nombre, String descripcion) {
            this.valor = valor;
            this.nombre = nombre;
            this.descripcion = descripcion;
        }

        public String getValor() {
            return valor;
        }

        public String getNombre() {
            return nombre;
        }

        public String getDescripcion() {
            return descripcion;
        }
    }

    private static final List<Opcion> DISENOS = Collections.unmodifiableList(Arrays.asList(
            new Opcion("tarjetas", "Tarjetas", "Cada platillo en su recuadro"),
            new Opcion("lista", "Lista", "Una carta clásica, con el precio al lado")));

    private static final List<Opcion> TIPOGRAFIAS = Collections.unmodifiableList(Arrays.asList(
            new Opcion("clasica", "Clásica", "Títulos con letra elegante"),
            new Opcion("moderna", "Moderna", "Letra limpia y sencilla"),
            new Opcion("calida", "Cálida", "Letra redondeada y amable")));

    private static final List<Opcion> ESQUINAS = Collections.unmodifiableList(Arrays.asList(
            new Opcion("redondas", "Redondeadas", "Esquinas suaves"),
            new Opcion("rectas", "Rectas", "Esquinas marcadas")));

    private static final List<Opcion> FONDOS = Collections.unmodifiableList(Arrays.asList(
            new Opcion("claro", "Claro", "Fondo crema"),
            new Opcion("oscuro", "Oscuro", "Fondo oscuro, bueno para la noche")));

    public static List<Opcion> disenos() {
        return DISENOS;
    }

    public static List<Opcion> tipografias() {
        return TIPOGRAFIAS;
    }

    public static List<Opcion> esquinas() {
        return ESQUINAS;
    }

    public static List<Opcion> fondos() {
        return FONDOS;
    }

    public static boolean esDisenoValido(String v) {
        return buscarOpcion(DISENOS, v) != null;
    }

    public static boolean esTipografiaValida(String v) {
        return buscarOpcion(TIPOGRAFIAS, v) != null;
    }

    public static boolean sonEsquinasValidas(String v) {
        return buscarOpcion(ESQUINAS, v) != null;
    }

    public static boolean esFondoValido(String v) {
        return buscarOpcion(FONDOS, v) != null;
    }

    public static String normalizarDiseno(String v) {
        return esDisenoValido(v) ? v.trim() : DISENO_PREDETERMINADO;
    }

    public static String normalizarTipografia(String v) {
        return esTipografiaValida(v) ? v.trim() : TIPOGRAFIA_PREDETERMINADA;
    }

    public static String normalizarEsquinas(String v) {
        return sonEsquinasValidas(v) ? v.trim() : ESQUINAS_PREDETERMINADAS;
    }

    public static String normalizarFondo(String v) {
        return esFondoValido(v) ? v.trim() : FONDO_PREDETERMINADO;
    }

    /**
     * Clases CSS del estilo (se ponen en el body del menú). Solo salen de las listas cerradas:
     * "mj-diseno-lista mj-tipo-calida mj-esq-rectas mj-fondo-oscuro".
     */
    public static String clases(String diseno, String tipografia, String esquinas, String fondo) {
        return "mj-diseno-" + normalizarDiseno(diseno) + " mj-tipo-" + normalizarTipografia(tipografia)
                + " mj-esq-" + normalizarEsquinas(esquinas) + " mj-fondo-" + normalizarFondo(fondo);
    }

    private static Opcion buscarOpcion(List<Opcion> lista, String valor) {
        if (valor == null) {
            return null;
        }
        String v = valor.trim();
        for (Opcion o : lista) {
            if (o.getValor().equals(v)) {
                return o;
            }
        }
        return null;
    }

    public static List<Tema> todos() {
        return TODOS;
    }

    public static boolean esValido(String hex) {
        return buscar(hex) != null;
    }

    /** El color de la paleta que corresponde al valor dado, o el predeterminado si no existe. */
    public static String normalizar(String hex) {
        Tema t = buscar(hex);
        return t == null ? PREDETERMINADO : t.getHex();
    }

    /** Versión más oscura del color (para botones al pasar el cursor). */
    public static String oscuro(String hex) {
        return mezclar(normalizar(hex), 0x000000, 0.22);
    }

    /** Versión aclarada del color (para textos de acento sobre fondo oscuro). */
    public static String claro(String hex) {
        return mezclar(normalizar(hex), 0xFFFFFF, 0.55);
    }

    /** Versión muy clara del color (para fondos suaves). */
    public static String suave(String hex) {
        return mezclar(normalizar(hex), 0xFFFFFF, 0.88);
    }

    /** Versión muy oscura del color (para el encabezado y el banner del menú). */
    public static String profundo(String hex) {
        return mezclar(normalizar(hex), 0x000000, 0.62);
    }

    /** Variables de color (CSS) de un tema con fondo claro. */
    public static String estilo(String hex) {
        return estilo(hex, FONDO_PREDETERMINADO);
    }

    /**
     * Variables de color (CSS) de un tema, para el atributo style de una página.
     * Solo contiene colores calculados de la paleta, nunca texto escrito por el usuario.
     * Con fondo oscuro, el color suave y el de los enlaces se ajustan para que se lean bien.
     */
    public static String estilo(String hex, String fondo) {
        String c = normalizar(hex);
        boolean oscuro = "oscuro".equals(normalizarFondo(fondo));
        return "--mj-accent:" + c + ";--mj-primary:" + c + ";--mj-primary-dark:" + oscuro(c)
                + ";--mj-tint:" + (oscuro ? mezclar(c, 0x000000, 0.72) : suave(c))
                + ";--mj-link:" + (oscuro ? claro(c) : oscuro(c))
                + ";--mj-dark:" + profundo(c)
                + ";--mj-accent-claro:" + claro(c) + ";";
    }

    private static Tema buscar(String hex) {
        if (hex == null) {
            return null;
        }
        String h = hex.trim().toUpperCase(Locale.ROOT);
        for (Tema t : TODOS) {
            if (t.getHex().equals(h)) {
                return t;
            }
        }
        return null;
    }

    /** Mezcla el color con otro (negro o blanco) en la proporción dada: 0 = igual, 1 = el otro. */
    private static String mezclar(String hex, int otro, double proporcion) {
        int c = Integer.parseInt(hex.substring(1), 16);
        int r = mezclarCanal((c >> 16) & 0xFF, (otro >> 16) & 0xFF, proporcion);
        int g = mezclarCanal((c >> 8) & 0xFF, (otro >> 8) & 0xFF, proporcion);
        int b = mezclarCanal(c & 0xFF, otro & 0xFF, proporcion);
        return String.format("#%02X%02X%02X", r, g, b);
    }

    private static int mezclarCanal(int base, int otro, double proporcion) {
        return (int) Math.round(base + (otro - base) * proporcion);
    }
}
