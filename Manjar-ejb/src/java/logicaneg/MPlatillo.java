package logicaneg;

import dao.CategoriaFacade;
import dao.EstablecimientoFacade;
import dao.PlatilloFacade;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;
import modelo.Categoria;
import modelo.Establecimiento;
import modelo.Platillo;

/**
 * Lógica del menú: HU-04 (gestión del menú del día), HU-05 (disponibilidad
 * automática), y consulta pública (HU-06 / HU-07).
 *
 * Regla de disponibilidad (HU-05):
 *   - CantidadEstimada = porciones que se esperan vender en el día.
 *     Si es 0, el platillo NO lleva control de porciones (siempre disponible
 *     salvo que el gerente lo marque como agotado a mano).
 *   - CantidadRestante baja con cada venta (comanda o apartado). Cuando llega
 *     a 0, el platillo pasa solo a "agotado".
 *   - "Reiniciar día" vuelve CantidadRestante = CantidadEstimada. Lo hace solo la
 *     apertura de caja (HU-11) y también está el botón manual.
 *
 * Todos los métodos de escritura reciben el id del establecimiento de la
 * SESIÓN y comprueban que el platillo sea de ese negocio.
 */
@Stateless
@LocalBean
public class MPlatillo {

    private static final BigDecimal PRECIO_MAXIMO = new BigDecimal("99999999.99");

    @PersistenceContext(unitName = "Manjar-ejbPU")
    private EntityManager em;

    @EJB
    private MAcceso mAcceso;

    @EJB
    private PlatilloFacade platilloFacade;

    @EJB
    private CategoriaFacade categoriaFacade;

    @EJB
    private EstablecimientoFacade establecimientoFacade;

    // ------------------------------------------------------------------
    // Consultas
    // ------------------------------------------------------------------
    public List<Platillo> listar(Integer establecimientoId) {
        return em.createQuery(
                "SELECT p FROM Platillo p WHERE p.establecimientoid.id = :e "
                + "ORDER BY p.categoriaid.nombre, p.nombre", Platillo.class)
                .setParameter("e", establecimientoId)
                .getResultList();
    }

    /** Los platillos que se pueden pedir ahora (disponibles), en orden de categoría y nombre. */
    public List<ItemMenu> disponibles(Integer establecimientoId) {
        List<Platillo> r = em.createQuery(
                "SELECT p FROM Platillo p WHERE p.establecimientoid.id = :e AND p.estado = :d "
                + "ORDER BY p.categoriaid.nombre, p.nombre", Platillo.class)
                .setParameter("e", establecimientoId)
                .setParameter("d", Constantes.PLATILLO_DISPONIBLE)
                .getResultList();
        List<ItemMenu> items = new java.util.ArrayList<>();
        for (Platillo p : r) {
            Integer restante = valor(p.getCantidadEstimada()) > 0 ? valor(p.getCantidadRestante()) : null;
            if (restante != null && restante == 0) {
                continue;
            }
            items.add(new ItemMenu(p.getId(), p.getNombre(), p.getDescripcion(),
                    p.getCategoriaid() == null ? "" : p.getCategoriaid().getNombre(), p.getPrecio(), restante));
        }
        return items;
    }

    /** Platillos de la sucursal para administrarlos (con costo y porciones): solo Dueño o Gerente de esa sucursal. */
    public List<Platillo> listar(Integer establecimientoId, Integer usuarioId) throws ReglaNegocioException {
        mAcceso.administrador(establecimientoId, usuarioId);
        return listar(establecimientoId);
    }

    public List<Categoria> listarCategorias(Integer establecimientoId, Integer usuarioId) throws ReglaNegocioException {
        mAcceso.administrador(establecimientoId, usuarioId);
        return listarCategorias(establecimientoId);
    }

    public List<Categoria> listarCategorias(Integer establecimientoId) {
        return em.createQuery(
                "SELECT c FROM Categoria c WHERE c.establecimientoid.id = :e AND c.estado = :a "
                + "ORDER BY c.nombre", Categoria.class)
                .setParameter("e", establecimientoId)
                .setParameter("a", Constantes.ESTADO_ACTIVO)
                .getResultList();
    }

    /** Cuántos platillos tiene la sucursal (para el panel). Lo puede pedir el personal de esa sucursal. */
    public long contar(Integer establecimientoId, Integer usuarioId) throws ReglaNegocioException {
        mAcceso.personal(establecimientoId, usuarioId);
        return em.createQuery("SELECT COUNT(p) FROM Platillo p WHERE p.establecimientoid.id = :e", Long.class)
                .setParameter("e", establecimientoId)
                .getSingleResult();
    }

    public long contarDisponibles(Integer establecimientoId, Integer usuarioId) throws ReglaNegocioException {
        mAcceso.personal(establecimientoId, usuarioId);
        return em.createQuery(
                "SELECT COUNT(p) FROM Platillo p WHERE p.establecimientoid.id = :e AND p.estado = :d", Long.class)
                .setParameter("e", establecimientoId)
                .setParameter("d", Constantes.PLATILLO_DISPONIBLE)
                .getSingleResult();
    }

    // ------------------------------------------------------------------
    // HU-04: crear / editar / eliminar
    // ------------------------------------------------------------------
    public Categoria crearCategoria(Integer establecimientoId, Integer usuarioId, String nombre) throws ReglaNegocioException {
        mAcceso.administrador(establecimientoId, usuarioId);
        return crearCategoria(establecimientoId, nombre);
    }

    private Categoria crearCategoria(Integer establecimientoId, String nombre) throws ReglaNegocioException {
        Establecimiento est = buscarEstablecimiento(establecimientoId);
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new ReglaNegocioException("Escribe el nombre de la categoría.");
        }
        String limpio = nombre.trim();
        if (limpio.length() > 255) {
            throw new ReglaNegocioException("El nombre de la categoría es demasiado largo.");
        }
        long repetidas = em.createQuery(
                "SELECT COUNT(c) FROM Categoria c WHERE c.establecimientoid.id = :e AND LOWER(c.nombre) = :n", Long.class)
                .setParameter("e", establecimientoId)
                .setParameter("n", limpio.toLowerCase())
                .getSingleResult();
        if (repetidas > 0) {
            throw new ReglaNegocioException("Ya existe una categoría con ese nombre.");
        }
        Categoria c = new Categoria();
        c.setNombre(limpio);
        c.setEstado(Constantes.ESTADO_ACTIVO);
        c.setEstablecimientoid(est);
        categoriaFacade.create(c);
        em.flush();
        return c;
    }

    /**
     * Crea el platillo (si datos.getId() es null) o actualiza el existente.
     *
     * @param disponible lo que marcó el usuario en el formulario; el sistema lo
     *                   corrige a "agotado" si el platillo lleva control de
     *                   porciones y ya no quedan.
     */
    public Platillo guardar(Integer establecimientoId, Integer usuarioId, Integer categoriaId, Platillo datos,
            boolean disponible) throws ReglaNegocioException {
        mAcceso.administrador(establecimientoId, usuarioId);
        return guardar(establecimientoId, categoriaId, datos, disponible);
    }

    private Platillo guardar(Integer establecimientoId, Integer categoriaId, Platillo datos, boolean disponible)
            throws ReglaNegocioException {
        Establecimiento est = buscarEstablecimiento(establecimientoId);

        // --- validaciones ---
        if (datos.getNombre() == null || datos.getNombre().trim().isEmpty()) {
            throw new ReglaNegocioException("Escribe el nombre del platillo.");
        }
        if (datos.getNombre().trim().length() > 255) {
            throw new ReglaNegocioException("El nombre del platillo es demasiado largo.");
        }
        if (datos.getDescripcion() != null && datos.getDescripcion().length() > 255) {
            throw new ReglaNegocioException("La descripción no puede pasar de 255 caracteres.");
        }
        if (datos.getPrecio() == null || datos.getPrecio().signum() < 0) {
            throw new ReglaNegocioException("El precio debe ser 0 o mayor.");
        }
        if (datos.getPrecio().compareTo(PRECIO_MAXIMO) > 0) {
            throw new ReglaNegocioException("El precio es demasiado grande.");
        }
        if (datos.getCosto() != null && (datos.getCosto().signum() < 0 || datos.getCosto().compareTo(PRECIO_MAXIMO) > 0)) {
            throw new ReglaNegocioException("El costo debe ser 0 o mayor.");
        }
        int estimada = datos.getCantidadEstimada() == null ? 0 : datos.getCantidadEstimada();
        if (estimada < 0) {
            throw new ReglaNegocioException("Las porciones del día no pueden ser negativas.");
        }
        Categoria categoria = categoriaId == null ? null : categoriaFacade.find(categoriaId);
        if (categoria == null || categoria.getEstablecimientoid() == null
                || !categoria.getEstablecimientoid().getId().equals(establecimientoId)) {
            throw new ReglaNegocioException("Elige una categoría de tu negocio.");
        }

        // --- crear o localizar ---
        boolean nuevo = datos.getId() == null;
        Platillo p;
        if (nuevo) {
            p = new Platillo();
            p.setEstablecimientoid(est);
        } else {
            p = platilloFacade.find(datos.getId());
            if (p == null || !pertenece(p, establecimientoId)) {
                throw new ReglaNegocioException("Ese platillo no pertenece a tu negocio.");
            }
        }

        // porciones restantes: al crear arrancan igual que las del día
        int restante;
        if (nuevo || datos.getCantidadRestante() == null) {
            restante = estimada;
        } else {
            restante = datos.getCantidadRestante();
        }
        if (restante < 0) {
            throw new ReglaNegocioException("Las porciones restantes no pueden ser negativas.");
        }
        if (estimada > 0 && restante > estimada) {
            restante = estimada;
        }
        boolean llevaControl = estimada > 0;
        boolean agotadoPorConteo = llevaControl && restante == 0;

        // --- copiar datos ---
        p.setNombre(datos.getNombre().trim());
        p.setDescripcion(datos.getDescripcion() == null || datos.getDescripcion().trim().isEmpty()
                ? null : datos.getDescripcion().trim());
        p.setPrecio(datos.getPrecio().setScale(2, RoundingMode.HALF_UP));
        p.setCosto(datos.getCosto() == null ? null : datos.getCosto().setScale(2, RoundingMode.HALF_UP));
        p.setCantidadEstimada(estimada);
        p.setCantidadRestante(restante);
        p.setCategoriaid(categoria);
        p.setEstado(disponible && !agotadoPorConteo ? Constantes.PLATILLO_DISPONIBLE : Constantes.PLATILLO_AGOTADO);

        if (nuevo) {
            platilloFacade.create(p);
            em.flush();
        } else {
            platilloFacade.edit(p);
        }
        return p;
    }

    public void eliminar(Integer establecimientoId, Integer usuarioId, Integer platilloId) throws ReglaNegocioException {
        mAcceso.administrador(establecimientoId, usuarioId);
        eliminar(establecimientoId, platilloId);
    }

    private void eliminar(Integer establecimientoId, Integer platilloId) throws ReglaNegocioException {
        Platillo p = buscarPlatilloDelNegocio(establecimientoId, platilloId);
        long enComandas = em.createQuery(
                "SELECT COUNT(d) FROM Detallecomanda d WHERE d.platilloid.id = :p", Long.class)
                .setParameter("p", platilloId).getSingleResult();
        long enApartados = em.createQuery(
                "SELECT COUNT(d) FROM Detalleapartado d WHERE d.platilloid.id = :p", Long.class)
                .setParameter("p", platilloId).getSingleResult();
        if (enComandas + enApartados > 0) {
            throw new ReglaNegocioException("No se puede eliminar: el platillo ya tiene pedidos. "
                    + "Márcalo como agotado para ocultarlo del menú.");
        }
        platilloFacade.remove(p);
    }

    // ------------------------------------------------------------------
    // HU-05: disponibilidad
    // ------------------------------------------------------------------
    /** El gerente marca un platillo como agotado (o vuelve a ponerlo disponible) a mano. */
    public Platillo alternarDisponibilidad(Integer establecimientoId, Integer usuarioId, Integer platilloId)
            throws ReglaNegocioException {
        mAcceso.administrador(establecimientoId, usuarioId);
        return alternarDisponibilidad(establecimientoId, platilloId);
    }

    private Platillo alternarDisponibilidad(Integer establecimientoId, Integer platilloId) throws ReglaNegocioException {
        Platillo p = buscarPlatilloDelNegocio(establecimientoId, platilloId);
        if (p.getEstado() != null && p.getEstado() == Constantes.PLATILLO_DISPONIBLE) {
            p.setEstado(Constantes.PLATILLO_AGOTADO);
        } else {
            if (llevaControl(p) && valor(p.getCantidadRestante()) == 0) {
                throw new ReglaNegocioException("No quedan porciones. Edita el platillo para agregar porciones "
                        + "o usa \"Reiniciar día\".");
            }
            p.setEstado(Constantes.PLATILLO_DISPONIBLE);
        }
        platilloFacade.edit(p);
        return p;
    }

    /**
     * Descuenta porciones vendidas (la usan las comandas, HU-08, y los apartados, HU-13).
     * Cuando llega a 0 el platillo pasa solo a "agotado".
     *
     * Bloquea la fila del platillo mientras dura la transacción: si dos meseros piden la última
     * porción al mismo tiempo, solo uno la consigue y el otro recibe el aviso de que se agotó.
     */
    public Platillo descontar(Integer establecimientoId, Integer platilloId, int cantidad) throws ReglaNegocioException {
        if (cantidad <= 0) {
            throw new ReglaNegocioException("La cantidad debe ser mayor que cero.");
        }
        Platillo p = buscarPlatilloBloqueado(establecimientoId, platilloId);
        if (p.getEstado() == null || p.getEstado() != Constantes.PLATILLO_DISPONIBLE) {
            throw new ReglaNegocioException("\"" + p.getNombre() + "\" está agotado.");
        }
        if (llevaControl(p)) {
            int restante = valor(p.getCantidadRestante());
            if (cantidad > restante) {
                throw new ReglaNegocioException("De \"" + p.getNombre() + "\" solo quedan " + restante
                        + (restante == 1 ? " porción." : " porciones."));
            }
            restante -= cantidad;
            p.setCantidadRestante(restante);
            if (restante == 0) {
                p.setEstado(Constantes.PLATILLO_AGOTADO);
            }
            platilloFacade.edit(p);
        }
        return p;
    }

    /**
     * Regresa porciones al menú (se canceló una comanda o venció un apartado).
     * Si el platillo se había agotado por conteo, vuelve a estar disponible; si el gerente lo
     * había marcado como agotado a mano (todavía quedaban porciones), se respeta.
     */
    public void devolver(Integer establecimientoId, Integer platilloId, int cantidad) throws ReglaNegocioException {
        if (cantidad <= 0) {
            return;
        }
        Platillo p = buscarPlatilloBloqueado(establecimientoId, platilloId);
        if (!llevaControl(p)) {
            return;
        }
        int antes = valor(p.getCantidadRestante());
        int despues = Math.min(valor(p.getCantidadEstimada()), antes + cantidad);
        p.setCantidadRestante(despues);
        if (antes == 0 && despues > 0) {
            p.setEstado(Constantes.PLATILLO_DISPONIBLE);
        }
        platilloFacade.edit(p);
    }

    /**
     * Nuevo día: las porciones restantes vuelven a las del día y se reabren los platillos con control.
     * Lo que ya está comprometido con apartados de hoy (esperando pago, pagados o listos) se descuenta, para no
     * vender dos veces la misma porción; si uno vence sin pago, sus porciones regresan al menú como siempre.
     */
    public int reiniciarDia(Integer establecimientoId, Integer usuarioId) throws ReglaNegocioException {
        mAcceso.administrador(establecimientoId, usuarioId);
        return reiniciarDia(establecimientoId);
    }

    /** Lo usa la apertura de caja (que ya validó a quien la abre). */
    public int reiniciarDia(Integer establecimientoId) throws ReglaNegocioException {
        buscarEstablecimiento(establecimientoId);
        Date inicio = MComanda.inicioDelDia(new Date());
        int reiniciados = 0;
        for (Platillo p : listar(establecimientoId)) {
            if (llevaControl(p)) {
                Number apartadas = (Number) em.createQuery(
                        "SELECT COALESCE(SUM(d.cantidad), 0) FROM Detalleapartado d WHERE d.platilloid.id = :p "
                        + "AND d.apartadoid.horarecoleccion >= :inicio AND d.apartadoid.estado IN (:pendiente, :pagado, :listo)")
                        .setParameter("p", p.getId()).setParameter("inicio", inicio)
                        .setParameter("pendiente", Constantes.APARTADO_PENDIENTE)
                        .setParameter("pagado", Constantes.APARTADO_PAGADO).setParameter("listo", Constantes.APARTADO_LISTO)
                        .getSingleResult();
                int libres = Math.max(0, valor(p.getCantidadEstimada()) - apartadas.intValue());
                p.setCantidadRestante(libres);
                p.setEstado(libres > 0 ? Constantes.PLATILLO_DISPONIBLE : Constantes.PLATILLO_AGOTADO);
                platilloFacade.edit(p);
                reiniciados++;
            }
        }
        return reiniciados;
    }

    // ------------------------------------------------------------------
    // Apoyo
    // ------------------------------------------------------------------
    private boolean llevaControl(Platillo p) {
        return valor(p.getCantidadEstimada()) > 0;
    }

    private int valor(Integer n) {
        return n == null ? 0 : n;
    }

    private boolean pertenece(Platillo p, Integer establecimientoId) {
        return p.getEstablecimientoid() != null && p.getEstablecimientoid().getId().equals(establecimientoId);
    }

    private Establecimiento buscarEstablecimiento(Integer id) throws ReglaNegocioException {
        Establecimiento est = id == null ? null : establecimientoFacade.find(id);
        if (est == null) {
            throw new ReglaNegocioException("No se encontró el negocio.");
        }
        return est;
    }

    /** Igual que buscarPlatilloDelNegocio, pero bloquea la fila hasta que termine la transacción. */
    private Platillo buscarPlatilloBloqueado(Integer establecimientoId, Integer platilloId) throws ReglaNegocioException {
        Platillo p = platilloId == null ? null : em.find(Platillo.class, platilloId, LockModeType.PESSIMISTIC_WRITE);
        if (p == null || !pertenece(p, establecimientoId)) {
            throw new ReglaNegocioException("Ese platillo no pertenece a tu negocio.");
        }
        return p;
    }

    private Platillo buscarPlatilloDelNegocio(Integer establecimientoId, Integer platilloId) throws ReglaNegocioException {
        Platillo p = platilloId == null ? null : platilloFacade.find(platilloId);
        if (p == null || !pertenece(p, establecimientoId)) {
            throw new ReglaNegocioException("Ese platillo no pertenece a tu negocio.");
        }
        return p;
    }
}
