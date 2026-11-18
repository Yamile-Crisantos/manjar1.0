package logicaneg;

import dao.EstablecimientoFacade;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import modelo.Establecimiento;

/**
 * Consultas de establecimientos (los "negocios" que usan Manjar).
 * Se usan en el portal público: lista de negocios y encabezado del menú.
 */
@Stateless
@LocalBean
public class MEstablecimiento {

    @PersistenceContext(unitName = "Manjar-ejbPU")
    private EntityManager em;

    @EJB
    private EstablecimientoFacade establecimientoFacade;

    public List<Establecimiento> listar() {
        return em.createQuery("SELECT e FROM Establecimiento e ORDER BY e.nombre", Establecimiento.class)
                .getResultList();
    }

    public Establecimiento buscar(Integer id) {
        return id == null ? null : establecimientoFacade.find(id);
    }
}
