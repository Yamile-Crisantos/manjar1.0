package logicaneg;

import java.util.ArrayList;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;
import modelo.Establecimiento;
import modelo.Usuario;

/**
 * HU-18 (registro de sucursales y asignación de Gerente) y HU-19 (el Gerente solo opera la suya).
 *
 * Una sucursal es un establecimiento más del mismo dueño (mismo DuenoId): tiene su propio menú, caja, comandas,
 * apartados, personal y código de QR, y ninguna sucursal ve los datos de otra. El dueño opera cualquiera de ellas
 * con su cuenta; un Gerente solo la que tiene asignada (lo comprueba MAcceso en cada operación).
 *
 * Si el dueño tiene una sola sucursal, la opera él mismo y no necesita crear una cuenta de Gerente.
 */
@Stateless
@LocalBean
public class MSucursal {

    public static final int MAX_SUCURSALES = 20;
    private static final int NOMBRE_MAX = 255;
    private static final int DIRECCION_MAX = 255;

    @PersistenceContext(unitName = "Manjar-ejbPU")
    private EntityManager em;

    @EJB
    private MAcceso mAcceso;

    @EJB
    private MUsuario mUsuario;

    @EJB
    private MEstablecimiento mEstablecimiento;

    // ------------------------------------------------------------------
    // Consulta
    // ------------------------------------------------------------------
    /** Las sucursales del dueño, la primera (la de su registro) al inicio. */
    public List<SucursalVista> listar(Integer duenoId) throws ReglaNegocioException {
        Usuario dueno = dueno(duenoId);
        List<SucursalVista> salida = new ArrayList<>();
        boolean primera = true;
        for (Establecimiento e : propias(dueno)) {
            salida.add(vista(e, primera));
            primera = false;
        }
        return salida;
    }

    /** Los gerentes activos del dueño, con su sucursal actual (para reasignarlos). */
    public List<Usuario> gerentes(Integer duenoId) throws ReglaNegocioException {
        Usuario dueno = dueno(duenoId);
        return em.createQuery(
                "SELECT u FROM Usuario u WHERE u.rol = :g AND u.estado = :a AND u.establecimientoid.duenoid.id = :d "
                + "ORDER BY u.nombre", Usuario.class)
                .setParameter("g", Constantes.ROL_GERENTE).setParameter("a", Constantes.ESTADO_ACTIVO)
                .setParameter("d", dueno.getId()).getResultList();
    }

    // ------------------------------------------------------------------
    // HU-18: registrar sucursales
    // ------------------------------------------------------------------
    public SucursalVista crear(Integer duenoId, String nombre, String direccion) throws ReglaNegocioException {
        Usuario dueno = dueno(duenoId);
        // Se bloquea la fila del dueño: dos altas a la vez no pueden pasar del máximo ni repetir el nombre
        em.find(Usuario.class, duenoId, LockModeType.PESSIMISTIC_WRITE);
        String n = limpio(nombre);
        if (n == null) {
            throw new ReglaNegocioException("Escribe el nombre de la sucursal.");
        }
        if (n.length() > NOMBRE_MAX) {
            throw new ReglaNegocioException("El nombre de la sucursal es demasiado largo.");
        }
        String dir = limpio(direccion);
        if (dir == null) {
            throw new ReglaNegocioException("Escribe la dirección de la sucursal.");
        }
        if (dir.length() > DIRECCION_MAX) {
            throw new ReglaNegocioException("La dirección es demasiado larga.");
        }
        List<Establecimiento> propias = propias(dueno);
        if (propias.size() >= MAX_SUCURSALES) {
            throw new ReglaNegocioException("Llegaste al máximo de " + MAX_SUCURSALES + " sucursales.");
        }
        for (Establecimiento e : propias) {
            if (e.getNombre() != null && e.getNombre().trim().equalsIgnoreCase(n)) {
                throw new ReglaNegocioException("Ya tienes una sucursal con ese nombre. Agrega la colonia o la calle para distinguirlas.");
            }
        }
        Establecimiento modelo = propias.isEmpty() ? null : propias.get(0);
        Establecimiento e = new Establecimiento();
        e.setNombre(n);
        e.setDireccion(dir);
        e.setDuenoid(dueno);
        // La sucursal nace con el mismo estilo de menú que la primera (se personaliza aparte)
        e.setColorMenu(modelo != null && modelo.getColorMenu() != null ? modelo.getColorMenu() : Temas.PREDETERMINADO);
        e.setDiseno(modelo != null && modelo.getDiseno() != null ? modelo.getDiseno() : Temas.DISENO_PREDETERMINADO);
        e.setTipografia(modelo != null && modelo.getTipografia() != null ? modelo.getTipografia() : Temas.TIPOGRAFIA_PREDETERMINADA);
        e.setEsquinas(modelo != null && modelo.getEsquinas() != null ? modelo.getEsquinas() : Temas.ESQUINAS_PREDETERMINADAS);
        e.setFondo(modelo != null && modelo.getFondo() != null ? modelo.getFondo() : Temas.FONDO_PREDETERMINADO);
        e.setLema(modelo == null ? null : modelo.getLema());
        e.setTenantId(mEstablecimiento.nuevoTenant(n));
        e.setCodigoQR(mEstablecimiento.nuevoCodigoQR(n));
        em.persist(e);
        em.flush();
        e.setSystemId(String.format("SYS-%04d", e.getId()));
        em.flush();
        return vista(e, false);
    }

    // ------------------------------------------------------------------
    // HU-18: asignar Gerente
    // ------------------------------------------------------------------
    /** Crea la cuenta de un Gerente nuevo y lo asigna a la sucursal. */
    public SucursalVista crearGerente(Integer duenoId, Integer sucursalId, String nombre, String correo, String clave)
            throws ReglaNegocioException {
        dueno(duenoId);
        mUsuario.crearPersonal(sucursalId, duenoId, nombre, correo, clave, Constantes.ROL_GERENTE);
        return vista(mAcceso.establecimiento(sucursalId), false);
    }

    /** Pasa a un Gerente que ya existe (de otra sucursal del mismo dueño) a esta sucursal. */
    public SucursalVista moverGerente(Integer duenoId, Integer gerenteId, Integer sucursalDestinoId)
            throws ReglaNegocioException {
        Usuario dueno = dueno(duenoId);
        em.find(Usuario.class, duenoId, LockModeType.PESSIMISTIC_WRITE);
        mAcceso.dueno(sucursalDestinoId, duenoId);
        Usuario g = gerenteId == null ? null : em.find(Usuario.class, gerenteId, LockModeType.PESSIMISTIC_WRITE);
        if (g == null || g.getRol() == null || g.getRol() != Constantes.ROL_GERENTE || g.getEstablecimientoid() == null
                || g.getEstablecimientoid().getDuenoid() == null
                || !g.getEstablecimientoid().getDuenoid().getId().equals(dueno.getId())) {
            throw new ReglaNegocioException("Ese gerente no pertenece a tus sucursales.");
        }
        if (g.getEstado() == null || g.getEstado() != Constantes.ESTADO_ACTIVO) {
            throw new ReglaNegocioException("Esa cuenta de gerente está desactivada. Actívala antes de asignarla.");
        }
        Integer origenId = g.getEstablecimientoid().getId();
        if (origenId.equals(sucursalDestinoId)) {
            throw new ReglaNegocioException("Ese gerente ya está asignado a esta sucursal.");
        }
        // Siempre se bloquea primero la sucursal de id menor, para no cruzar bloqueos con otra operación
        Establecimiento origen;
        Establecimiento destino;
        if (origenId < sucursalDestinoId) {
            origen = em.find(Establecimiento.class, origenId, LockModeType.PESSIMISTIC_WRITE);
            destino = em.find(Establecimiento.class, sucursalDestinoId, LockModeType.PESSIMISTIC_WRITE);
        } else {
            destino = em.find(Establecimiento.class, sucursalDestinoId, LockModeType.PESSIMISTIC_WRITE);
            origen = em.find(Establecimiento.class, origenId, LockModeType.PESSIMISTIC_WRITE);
        }
        Usuario actual = destino.getGerenteid();
        if (actual != null && actual.getEstado() != null && actual.getEstado() == Constantes.ESTADO_ACTIVO) {
            throw new ReglaNegocioException("Esa sucursal ya tiene un gerente activo (" + actual.getNombre()
                    + "). Desactiva su cuenta en Personal antes de asignar a otro.");
        }
        if (origen.getGerenteid() != null && origen.getGerenteid().getId().equals(g.getId())) {
            origen.setGerenteid(null);
        }
        g.setEstablecimientoid(destino);
        destino.setGerenteid(g);
        em.flush();
        return vista(destino, false);
    }

    // ------------------------------------------------------------------
    // Operar una sucursal u otra (solo el dueño)
    // ------------------------------------------------------------------
    /** Comprueba que el dueño puede operar esa sucursal y devuelve sus datos para ponerla como la activa. */
    public SucursalVista operar(Integer duenoId, Integer sucursalId) throws ReglaNegocioException {
        dueno(duenoId);
        mAcceso.dueno(sucursalId, duenoId);
        return vista(mAcceso.establecimiento(sucursalId), false);
    }

    // ------------------------------------------------------------------
    // Apoyo
    // ------------------------------------------------------------------
    private Usuario dueno(Integer duenoId) throws ReglaNegocioException {
        Usuario u = mAcceso.usuarioActivo(duenoId);
        if (u.getRol() == null || u.getRol() != Constantes.ROL_DUENO) {
            throw new ReglaNegocioException("Solo el dueño puede hacer esto.");
        }
        return u;
    }

    private List<Establecimiento> propias(Usuario dueno) {
        return em.createQuery("SELECT e FROM Establecimiento e WHERE e.duenoid.id = :d ORDER BY e.id", Establecimiento.class)
                .setParameter("d", dueno.getId()).getResultList();
    }

    private SucursalVista vista(Establecimiento e, boolean principal) {
        SucursalVista v = new SucursalVista();
        v.setId(e.getId());
        v.setNombre(e.getNombre());
        v.setDireccion(e.getDireccion());
        v.setCodigoQR(e.getCodigoQR());
        v.setPrincipal(principal);
        Usuario g = e.getGerenteid();
        if (g != null) {
            v.setGerenteId(g.getId());
            v.setGerenteNombre(g.getNombre());
            v.setGerenteCorreo(g.getCorreo());
            v.setGerenteActivo(g.getEstado() != null && g.getEstado() == Constantes.ESTADO_ACTIVO);
        }
        v.setMeseros(em.createQuery(
                "SELECT COUNT(u) FROM Usuario u WHERE u.establecimientoid.id = :e AND u.rol = :m AND u.estado = :a", Long.class)
                .setParameter("e", e.getId()).setParameter("m", Constantes.ROL_MESERO)
                .setParameter("a", Constantes.ESTADO_ACTIVO).getSingleResult());
        return v;
    }

    private static String limpio(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
