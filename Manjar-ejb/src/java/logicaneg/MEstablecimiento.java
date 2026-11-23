package logicaneg;

import dao.EstablecimientoFacade;
import java.security.SecureRandom;
import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import modelo.Establecimiento;
import modelo.Usuario;

/**
 * Negocios (establecimientos) que contratan Manjar.
 *
 * Aquí está:
 *  - el registro de un negocio nuevo junto con la cuenta de su dueño;
 *  - la personalización del negocio (datos generales, color y estilo del menú, contacto y aviso del día);
 *  - el código del QR: cada negocio tiene un código corto y difícil de adivinar
 *    (campo CodigoQR) y su menú público se abre con  /vistas/menu.xhtml?q=<código>.
 *    Así el cliente llega al menú del negocio por su QR, sin lista de negocios.
 *
 * Nota: en la documentación del proyecto, el alta de un cliente nuevo (tenant) la hace la
 * plataforma de InToGlobe. Mientras esa parte no exista, Manjar lo da de alta directamente.
 */
@Stateless
@LocalBean
public class MEstablecimiento {

    /** Formato de un código de QR: minúsculas, números y guiones (nunca símbolos raros). */
    private static final Pattern CODIGO = Pattern.compile("^[a-z0-9]+(-[a-z0-9]+)*$");
    private static final int CODIGO_MIN = 4;
    private static final int CODIGO_MAX = 60;

    /** Sin letras que se confundan entre sí (0/o, 1/l/i). */
    private static final String ALFABETO = "abcdefghjkmnpqrstuvwxyz23456789";
    private static final int LARGO_ALEATORIO = 6;
    private static final SecureRandom ALEATORIO = new SecureRandom();

    private static final int NOMBRE_MAX = 255;
    private static final int DIRECCION_MAX = 255;
    private static final int LEMA_MAX = 120;
    private static final int HORARIO_MAX = 255;
    private static final int AVISO_MAX = 160;

    @PersistenceContext(unitName = "Manjar-ejbPU")
    private EntityManager em;

    @EJB
    private EstablecimientoFacade establecimientoFacade;

    @EJB
    private MUsuario mUsuario;

    @EJB
    private MAcceso mAcceso;

    // ------------------------------------------------------------------
    // Consultas
    // ------------------------------------------------------------------
    public Establecimiento buscar(Integer id) {
        return id == null ? null : establecimientoFacade.find(id);
    }

    /** El negocio al que pertenece un código de QR, o null si el código no existe o no tiene el formato correcto. */
    public Establecimiento buscarPorCodigoQR(String codigo) {
        if (!codigoConFormato(codigo)) {
            return null;
        }
        List<Establecimiento> r = em.createQuery(
                "SELECT e FROM Establecimiento e WHERE e.codigoQR = :c", Establecimiento.class)
                .setParameter("c", codigo)
                .setMaxResults(1)
                .getResultList();
        return r.isEmpty() ? null : r.get(0);
    }

    public static boolean codigoConFormato(String codigo) {
        return codigo != null && codigo.length() >= CODIGO_MIN && codigo.length() <= CODIGO_MAX
                && CODIGO.matcher(codigo).matches();
    }

    /**
     * Devuelve el código de QR del negocio. Si todavía no tenía (negocios anteriores a esta
     * versión), se genera en ese momento y se guarda.
     */
    public String codigoQR(Integer establecimientoId) {
        Establecimiento e = buscar(establecimientoId);
        if (e == null) {
            return null;
        }
        if (!codigoConFormato(e.getCodigoQR())) {
            e.setCodigoQR(generarCodigoUnico(e.getNombre()));
            establecimientoFacade.edit(e);
        }
        return e.getCodigoQR();
    }

    // ------------------------------------------------------------------
    // Pantalla de cocina (HU-10): la pantalla compartida entra con una clave por sucursal, sin cuenta
    // ------------------------------------------------------------------
    /** Una clave de cocina válida: 32 caracteres hexadecimales. */
    public static boolean tokenCocinaConFormato(String token) {
        return token != null && token.length() == 32 && token.matches("[0-9a-f]{32}");
    }

    /** La clave de la pantalla de cocina de la sucursal; se genera la primera vez que se pide. */
    public String tokenCocina(Integer establecimientoId, Integer usuarioId) throws ReglaNegocioException {
        mAcceso.administrador(establecimientoId, usuarioId);
        Establecimiento e = buscar(establecimientoId);
        if (e == null) {
            return null;
        }
        if (!tokenCocinaConFormato(e.getTokenCocina())) {
            e.setTokenCocina(nuevoTokenCocina());
            establecimientoFacade.edit(e);
        }
        return e.getTokenCocina();
    }

    /** Cambia la clave (por ejemplo si se perdió una tablet): el enlace anterior deja de funcionar. */
    public String regenerarTokenCocina(Integer establecimientoId, Integer usuarioId) throws ReglaNegocioException {
        mAcceso.administrador(establecimientoId, usuarioId);
        Establecimiento e = buscar(establecimientoId);
        if (e == null) {
            throw new ReglaNegocioException("No se encontró la sucursal.");
        }
        e.setTokenCocina(nuevoTokenCocina());
        establecimientoFacade.edit(e);
        return e.getTokenCocina();
    }

    /** La sucursal a la que pertenece una clave de cocina, o null si no existe o no tiene el formato correcto. */
    public Establecimiento buscarPorTokenCocina(String token) {
        if (!tokenCocinaConFormato(token)) {
            return null;
        }
        List<Establecimiento> r = em.createQuery(
                "SELECT e FROM Establecimiento e WHERE e.tokenCocina = :t", Establecimiento.class)
                .setParameter("t", token)
                .setMaxResults(1)
                .getResultList();
        return r.isEmpty() ? null : r.get(0);
    }

    private String nuevoTokenCocina() {
        byte[] b = new byte[16];
        ALEATORIO.nextBytes(b);
        StringBuilder sb = new StringBuilder(32);
        for (byte x : b) {
            sb.append(Character.forDigit((x >> 4) & 0xF, 16)).append(Character.forDigit(x & 0xF, 16));
        }
        return sb.toString();
    }

    // ------------------------------------------------------------------
    // Registro de un negocio nuevo
    // ------------------------------------------------------------------
    /**
     * Crea la cuenta del dueño y su negocio en una sola operación (una transacción:
     * si algo falla no se guarda nada). El negocio nace con el color predeterminado y su código de QR.
     *
     * @return el dueño, ya ligado a su negocio
     */
    public Usuario registrarNegocio(String nombreNegocio, String direccion, String nombreDueno,
            String correo, String clave) throws ReglaNegocioException {
        String negocio = texto(nombreNegocio);
        if (negocio == null) {
            throw new ReglaNegocioException("Escribe el nombre de tu negocio.");
        }
        if (negocio.length() > NOMBRE_MAX) {
            throw new ReglaNegocioException("El nombre del negocio es demasiado largo.");
        }
        String dir = texto(direccion);
        if (dir != null && dir.length() > DIRECCION_MAX) {
            throw new ReglaNegocioException("La dirección es demasiado larga.");
        }

        // Valida y crea la cuenta del dueño (nombre, correo, contraseña, correo repetido)
        Usuario dueno = mUsuario.registrarDueno(nombreDueno, correo, clave);

        Establecimiento e = new Establecimiento();
        e.setNombre(negocio);
        e.setDireccion(dir);
        e.setDuenoid(dueno);
        e.setColorMenu(Temas.PREDETERMINADO);
        e.setDiseno(Temas.DISENO_PREDETERMINADO);
        e.setTipografia(Temas.TIPOGRAFIA_PREDETERMINADA);
        e.setEsquinas(Temas.ESQUINAS_PREDETERMINADAS);
        e.setFondo(Temas.FONDO_PREDETERMINADO);
        e.setTenantId(generarTenantUnico(negocio));
        e.setCodigoQR(generarCodigoUnico(negocio));
        establecimientoFacade.create(e);
        em.flush();
        e.setSystemId(String.format("SYS-%04d", e.getId()));

        dueno.setEstablecimientoid(e);
        em.flush();
        return dueno;
    }

    // ------------------------------------------------------------------
    // Personalización
    // ------------------------------------------------------------------
    /**
     * Actualiza lo que el dueño controla de su negocio: datos generales, cómo se ve su menú público
     * (color y estilo), contacto y aviso del día. Todo se valida aquí, no solo en la pantalla.
     */
    public Establecimiento actualizarNegocio(Integer establecimientoId, Integer duenoId, DatosNegocio d)
            throws ReglaNegocioException {
        mAcceso.dueno(establecimientoId, duenoId);
        Establecimiento e = buscar(establecimientoId);
        if (e == null) {
            throw new ReglaNegocioException("No se encontró el negocio.");
        }
        String n = texto(d.getNombre());
        if (n == null) {
            throw new ReglaNegocioException("Escribe el nombre de tu negocio.");
        }
        if (n.length() > NOMBRE_MAX) {
            throw new ReglaNegocioException("El nombre del negocio es demasiado largo.");
        }
        String dir = limitado(d.getDireccion(), DIRECCION_MAX, "La dirección es demasiado larga.");
        String lema = limitado(d.getLema(), LEMA_MAX, "El lema puede tener hasta " + LEMA_MAX + " caracteres.");
        String horario = limitado(d.getHorario(), HORARIO_MAX, "El horario puede tener hasta " + HORARIO_MAX + " caracteres.");
        String aviso = limitado(d.getAviso(), AVISO_MAX, "El aviso puede tener hasta " + AVISO_MAX + " caracteres.");

        if (!Temas.esValido(d.getColor())) {
            throw new ReglaNegocioException("Elige uno de los colores disponibles.");
        }
        if (!Temas.esDisenoValido(d.getDiseno()) || !Temas.esTipografiaValida(d.getTipografia())
                || !Temas.sonEsquinasValidas(d.getEsquinas()) || !Temas.esFondoValido(d.getFondo())) {
            throw new ReglaNegocioException("Elige un estilo de la lista para tu menú.");
        }

        String telefono = Contacto.normalizarTelefono(d.getTelefono(), "teléfono");
        String whatsapp = Contacto.normalizarWhatsapp(d.getWhatsapp(), "número de WhatsApp");
        String instagram = Contacto.normalizarInstagram(d.getInstagram());
        String facebook = Contacto.normalizarFacebook(d.getFacebook());

        e.setNombre(n);
        e.setDireccion(dir);
        e.setLema(lema);
        e.setColorMenu(Temas.normalizar(d.getColor()));
        e.setDiseno(Temas.normalizarDiseno(d.getDiseno()));
        e.setTipografia(Temas.normalizarTipografia(d.getTipografia()));
        e.setEsquinas(Temas.normalizarEsquinas(d.getEsquinas()));
        e.setFondo(Temas.normalizarFondo(d.getFondo()));
        e.setTelefono(telefono);
        e.setWhatsapp(whatsapp);
        e.setHorario(horario);
        e.setInstagram(instagram);
        e.setFacebook(facebook);
        e.setAviso(aviso);
        establecimientoFacade.edit(e);
        return e;
    }

    // ------------------------------------------------------------------
    // Apoyo
    // ------------------------------------------------------------------
    /** Texto sin espacios sobrantes (null si queda vacío); falla con el mensaje dado si pasa del máximo. */
    private static String limitado(String s, int maximo, String mensaje) throws ReglaNegocioException {
        String t = texto(s);
        if (t != null && t.length() > maximo) {
            throw new ReglaNegocioException(mensaje);
        }
        return t;
    }

    /** Texto sin espacios sobrantes; null si queda vacío. */
    private static String texto(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    /** "Fonda Doña Lupe" -> "fonda-dona-lupe" (sin acentos, solo minúsculas, números y guiones). */
    static String slug(String nombre, int maximo) {
        String s = nombre == null ? "" : Normalizer.normalize(nombre, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
        if (s.length() > maximo) {
            s = s.substring(0, maximo).replaceAll("-+$", "");
        }
        return s.isEmpty() ? "negocio" : s;
    }

    /** Identificador de tenant que todavía no usa ningún otro negocio o sucursal (lo usa MSucursal). */
    public String nuevoTenant(String nombre) {
        return generarTenantUnico(nombre);
    }

    /** Código de QR que todavía no usa ningún otro negocio o sucursal (lo usa MSucursal). */
    public String nuevoCodigoQR(String nombre) {
        return generarCodigoUnico(nombre);
    }

    private String generarTenantUnico(String nombre) {
        String base = slug(nombre, 40);
        String candidato = base;
        int n = 2;
        while (existeTenant(candidato)) {
            candidato = base + "-" + n++;
        }
        return candidato;
    }

    private boolean existeTenant(String tenant) {
        Long c = em.createQuery("SELECT COUNT(e) FROM Establecimiento e WHERE e.tenantId = :t", Long.class)
                .setParameter("t", tenant).getSingleResult();
        return c > 0;
    }

    /** Código con el nombre del negocio y 6 caracteres al azar: "fonda-dona-lupe-x7k2m9". */
    private String generarCodigoUnico(String nombre) {
        String base = slug(nombre, 24);
        String candidato;
        do {
            StringBuilder sb = new StringBuilder(base).append('-');
            for (int i = 0; i < LARGO_ALEATORIO; i++) {
                sb.append(ALFABETO.charAt(ALEATORIO.nextInt(ALFABETO.length())));
            }
            candidato = sb.toString();
        } while (existeCodigo(candidato));
        return candidato;
    }

    private boolean existeCodigo(String codigo) {
        Long c = em.createQuery("SELECT COUNT(e) FROM Establecimiento e WHERE e.codigoQR = :c", Long.class)
                .setParameter("c", codigo).getSingleResult();
        return c > 0;
    }
}
