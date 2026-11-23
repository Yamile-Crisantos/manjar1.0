package logicaneg;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import modelo.Establecimiento;
import modelo.Usuario;

/**
 * Comprobaciones de "¿esta persona puede operar esta sucursal?". Las usan comandas, caja, apartados,
 * sucursales y reportes, para no confiar solo en lo que diga la pantalla (el id de la sucursal y del
 * usuario vienen de la sesión, pero aquí se vuelve a revisar contra la base de datos).
 *
 *  - El Gerente y el Mesero solo operan la sucursal a la que están asignados (HU-19).
 *  - El Dueño opera cualquiera de las sucursales de su negocio (las que tienen su DuenoId).
 */
@Stateless
@LocalBean
public class MAcceso {

    @PersistenceContext(unitName = "Manjar-ejbPU")
    private EntityManager em;

    /** Dueño, Gerente o Mesero activo que trabaja en esa sucursal. */
    public Usuario personal(Integer establecimientoId, Integer usuarioId) throws ReglaNegocioException {
        Usuario u = usuarioActivo(usuarioId);
        Establecimiento est = establecimiento(establecimientoId);
        if (u.getRol() == null || u.getRol() == Constantes.ROL_CLIENTE || !trabajaEn(u, est)) {
            throw new ReglaNegocioException("No tienes permiso para operar esta sucursal.");
        }
        return u;
    }

    /** Dueño o Gerente activo de esa sucursal. */
    public Usuario administrador(Integer establecimientoId, Integer usuarioId) throws ReglaNegocioException {
        Usuario u = personal(establecimientoId, usuarioId);
        if (u.getRol() != Constantes.ROL_DUENO && u.getRol() != Constantes.ROL_GERENTE) {
            throw new ReglaNegocioException("Solo el dueño o el gerente pueden hacer esto.");
        }
        return u;
    }

    /** El dueño activo de esa sucursal (para sucursales, personal y reportes). */
    public Usuario dueno(Integer establecimientoId, Integer usuarioId) throws ReglaNegocioException {
        Usuario u = personal(establecimientoId, usuarioId);
        if (u.getRol() != Constantes.ROL_DUENO) {
            throw new ReglaNegocioException("Solo el dueño puede hacer esto.");
        }
        return u;
    }

    /** Un cliente activo. */
    public Usuario cliente(Integer usuarioId) throws ReglaNegocioException {
        Usuario u = usuarioActivo(usuarioId);
        if (u.getRol() == null || u.getRol() != Constantes.ROL_CLIENTE) {
            throw new ReglaNegocioException("Solo un cliente puede hacer esto.");
        }
        return u;
    }

    public Usuario usuarioActivo(Integer usuarioId) throws ReglaNegocioException {
        Usuario u = usuarioId == null ? null : em.find(Usuario.class, usuarioId);
        if (u == null || u.getEstado() == null || u.getEstado() != Constantes.ESTADO_ACTIVO) {
            throw new ReglaNegocioException("Tu cuenta no está activa. Inicia sesión de nuevo.");
        }
        return u;
    }

    public Establecimiento establecimiento(Integer establecimientoId) throws ReglaNegocioException {
        Establecimiento est = establecimientoId == null ? null : em.find(Establecimiento.class, establecimientoId);
        if (est == null) {
            throw new ReglaNegocioException("No se encontró la sucursal.");
        }
        return est;
    }

    private boolean trabajaEn(Usuario u, Establecimiento est) {
        if (u.getEstablecimientoid() != null && u.getEstablecimientoid().getId().equals(est.getId())) {
            return true;
        }
        return u.getRol() == Constantes.ROL_DUENO && est.getDuenoid() != null
                && est.getDuenoid().getId().equals(u.getId());
    }
}
