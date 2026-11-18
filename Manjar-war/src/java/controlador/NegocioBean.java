package controlador;

import java.io.Serializable;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import logicaneg.DatosNegocio;
import logicaneg.MEstablecimiento;
import logicaneg.ReglaNegocioException;
import modelo.Establecimiento;

/**
 * "Mi negocio": el dueño personaliza su negocio y cómo se ve su menú público
 * (datos generales, color y estilo, contacto y aviso del día).
 * Siempre trabaja sobre SU negocio (el de la sesión), nunca sobre uno elegido en la URL.
 */
@Named("negocioBean")
@ViewScoped
public class NegocioBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private MEstablecimiento mEstablecimiento;

    @Inject
    private SesionBean sesion;

    private DatosNegocio datos = new DatosNegocio();
    private boolean cargado;

    @PostConstruct
    public void cargar() {
        Establecimiento e = mEstablecimiento.buscar(sesion.getEstablecimientoId());
        if (e != null) {
            datos = DatosNegocio.de(e);
            cargado = true;
        }
    }

    public String guardar() {
        try {
            Establecimiento e = mEstablecimiento.actualizarNegocio(sesion.getEstablecimientoId(), datos);
            sesion.actualizarNombreNegocio(e.getNombre());
            // Se vuelve a leer lo guardado (así se ven los datos ya normalizados, por ejemplo el usuario de Instagram sin @)
            datos = DatosNegocio.de(e);
            Mensajes.info("Cambios guardados. Tu menú público ya los muestra.");
        } catch (ReglaNegocioException ex) {
            Mensajes.error(ex.getMessage());
        }
        return null;
    }

    /** Lo que se está editando. */
    public DatosNegocio getDatos() {
        return datos;
    }

    /** false si la cuenta no tiene un negocio asignado. */
    public boolean isCargado() {
        return cargado;
    }
}
