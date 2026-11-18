package logicaneg;

import dao.EstablecimientoFacade;
import dao.UsuarioFacade;
import java.util.List;
import java.util.regex.Pattern;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import modelo.Establecimiento;
import modelo.Usuario;

/**
 * Lógica de cuentas: HU-01 (registro y autenticación de cliente),
 * HU-02 (alta de cuentas de personal) y HU-03 (inicio de sesión de mesero).
 * También crea la cuenta del dueño cuando un negocio nuevo se registra en Manjar.
 *
 * Todos los métodos que reciben el id del establecimiento lo usan para
 * comprobar que el usuario pertenece a ESE negocio (aislamiento multi-tenant).
 */
@Stateless
@LocalBean
public class MUsuario {

    private static final Pattern CORREO = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final int CLAVE_MINIMA = 8;

    /** Hash de mentira: se usa cuando el correo no existe para tardar lo mismo que con un correo válido. */
    private static final String HASH_FALSO = ClaveUtil.generarHash("no-existe");

    @PersistenceContext(unitName = "Manjar-ejbPU")
    private EntityManager em;

    @EJB
    private UsuarioFacade usuarioFacade;

    @EJB
    private EstablecimientoFacade establecimientoFacade;

    // ------------------------------------------------------------------
    // Inicio de sesión
    // ------------------------------------------------------------------
    /**
     * Comprueba correo y contraseña. Devuelve el usuario o lanza
     * ReglaNegocioException con un mensaje para mostrar.
     */
    public Usuario autenticar(String correo, String clave) throws ReglaNegocioException {
        Usuario u = buscarPorCorreo(correo);
        String hash = (u == null) ? HASH_FALSO : u.getContrasenahash();
        boolean claveCorrecta = ClaveUtil.verificar(clave, hash);
        // Mismo mensaje si falla el correo o la contraseña: no revelamos cuál fue
        if (u == null || !claveCorrecta) {
            throw new ReglaNegocioException("Correo o contraseña incorrectos.");
        }
        if (u.getEstado() == null || u.getEstado() != Constantes.ESTADO_ACTIVO) {
            throw new ReglaNegocioException("Tu cuenta está desactivada. Consulta con el dueño del negocio.");
        }
        return u;
    }

    // ------------------------------------------------------------------
    // HU-01: registro del cliente (portal público)
    // ------------------------------------------------------------------
    public Usuario registrarCliente(String nombre, String correo, String clave) throws ReglaNegocioException {
        // El cliente no pertenece a ningún negocio: establecimientoid queda en NULL
        return crearCuenta(nombre, correo, clave, Constantes.ROL_CLIENTE);
    }

    // ------------------------------------------------------------------
    // Alta del dueño al registrar su negocio (la usa MEstablecimiento.registrarNegocio,
    // dentro de la misma transacción: si algo falla, no queda ninguna de las dos cosas)
    // ------------------------------------------------------------------
    public Usuario registrarDueno(String nombre, String correo, String clave) throws ReglaNegocioException {
        return crearCuenta(nombre, correo, clave, Constantes.ROL_DUENO);
    }

    private Usuario crearCuenta(String nombre, String correo, String clave, int rol) throws ReglaNegocioException {
        validarDatosCuenta(nombre, correo, clave);
        Usuario u = new Usuario();
        u.setNombre(nombre.trim());
        u.setCorreo(normalizarCorreo(correo));
        u.setContrasenahash(ClaveUtil.generarHash(clave));
        u.setRol(rol);
        u.setEstado(Constantes.ESTADO_ACTIVO);
        usuarioFacade.create(u);
        em.flush();
        return u;
    }

    // ------------------------------------------------------------------
    // HU-02: el dueño da de alta cuentas de personal
    // ------------------------------------------------------------------
    public Usuario crearPersonal(Integer establecimientoId, String nombre, String correo, String clave, int rol)
            throws ReglaNegocioException {
        if (rol != Constantes.ROL_MESERO && rol != Constantes.ROL_GERENTE) {
            throw new ReglaNegocioException("Solo se pueden crear cuentas de Gerente o Mesero.");
        }
        Establecimiento est = establecimientoFacade.find(establecimientoId);
        if (est == null) {
            throw new ReglaNegocioException("No se encontró el negocio.");
        }
        validarDatosCuenta(nombre, correo, clave);
        if (rol == Constantes.ROL_GERENTE && tieneGerenteActivo(est)) {
            throw new ReglaNegocioException("Este negocio ya tiene un gerente activo. Desactívalo antes de crear otro.");
        }
        Usuario u = new Usuario();
        u.setNombre(nombre.trim());
        u.setCorreo(normalizarCorreo(correo));
        u.setContrasenahash(ClaveUtil.generarHash(clave));
        u.setRol(rol);
        u.setEstado(Constantes.ESTADO_ACTIVO);
        u.setEstablecimientoid(est);
        usuarioFacade.create(u);
        em.flush();
        if (rol == Constantes.ROL_GERENTE) {
            est.setGerenteid(u);
            establecimientoFacade.edit(est);
        }
        return u;
    }

    public List<Usuario> listarPersonal(Integer establecimientoId) {
        return em.createQuery(
                "SELECT u FROM Usuario u WHERE u.establecimientoid.id = :e AND u.rol IN (:g, :m) "
                + "ORDER BY u.rol, u.nombre", Usuario.class)
                .setParameter("e", establecimientoId)
                .setParameter("g", Constantes.ROL_GERENTE)
                .setParameter("m", Constantes.ROL_MESERO)
                .getResultList();
    }

    public long contarPersonalActivo(Integer establecimientoId) {
        return em.createQuery(
                "SELECT COUNT(u) FROM Usuario u WHERE u.establecimientoid.id = :e AND u.rol IN (:g, :m) "
                + "AND u.estado = :a", Long.class)
                .setParameter("e", establecimientoId)
                .setParameter("g", Constantes.ROL_GERENTE)
                .setParameter("m", Constantes.ROL_MESERO)
                .setParameter("a", Constantes.ESTADO_ACTIVO)
                .getSingleResult();
    }

    /** Activa o desactiva una cuenta de personal (no se borran: queda el historial). */
    public void cambiarEstado(Integer establecimientoId, Integer usuarioId, boolean activo) throws ReglaNegocioException {
        Usuario u = usuarioFacade.find(usuarioId);
        boolean esPersonalDelNegocio = u != null
                && u.getEstablecimientoid() != null
                && u.getEstablecimientoid().getId().equals(establecimientoId)
                && u.getRol() != null
                && (u.getRol() == Constantes.ROL_GERENTE || u.getRol() == Constantes.ROL_MESERO);
        if (!esPersonalDelNegocio) {
            throw new ReglaNegocioException("Esa cuenta no pertenece a tu negocio.");
        }
        Establecimiento est = u.getEstablecimientoid();
        if (u.getRol() == Constantes.ROL_GERENTE) {
            if (activo) {
                if (tieneGerenteActivo(est) && !u.equals(est.getGerenteid())) {
                    throw new ReglaNegocioException("Ya hay otro gerente activo en este negocio.");
                }
                est.setGerenteid(u);
            } else if (u.equals(est.getGerenteid())) {
                est.setGerenteid(null);
            }
            establecimientoFacade.edit(est);
        }
        u.setEstado(activo ? Constantes.ESTADO_ACTIVO : Constantes.ESTADO_INACTIVO);
        usuarioFacade.edit(u);
    }

    // ------------------------------------------------------------------
    // Apoyo
    // ------------------------------------------------------------------
    public Usuario buscarPorCorreo(String correo) {
        if (correo == null || correo.trim().isEmpty()) {
            return null;
        }
        List<Usuario> r = em.createQuery("SELECT u FROM Usuario u WHERE LOWER(u.correo) = :c", Usuario.class)
                .setParameter("c", normalizarCorreo(correo))
                .setMaxResults(1)
                .getResultList();
        return r.isEmpty() ? null : r.get(0);
    }

    private boolean tieneGerenteActivo(Establecimiento est) {
        Usuario g = est.getGerenteid();
        return g != null && g.getEstado() != null && g.getEstado() == Constantes.ESTADO_ACTIVO;
    }

    private String normalizarCorreo(String correo) {
        return correo.trim().toLowerCase();
    }

    private void validarDatosCuenta(String nombre, String correo, String clave) throws ReglaNegocioException {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new ReglaNegocioException("Escribe el nombre.");
        }
        if (nombre.trim().length() > 255) {
            throw new ReglaNegocioException("El nombre es demasiado largo.");
        }
        if (correo == null || !CORREO.matcher(correo.trim()).matches() || correo.trim().length() > 255) {
            throw new ReglaNegocioException("Escribe un correo válido.");
        }
        if (clave == null || clave.length() < CLAVE_MINIMA) {
            throw new ReglaNegocioException("La contraseña debe tener al menos " + CLAVE_MINIMA + " caracteres.");
        }
        if (buscarPorCorreo(correo) != null) {
            throw new ReglaNegocioException("Ya existe una cuenta con ese correo.");
        }
    }
}
