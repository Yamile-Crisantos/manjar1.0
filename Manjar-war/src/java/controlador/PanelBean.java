package controlador;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import logicaneg.MPlatillo;
import logicaneg.MUsuario;

/**
 * Datos del panel de inicio de cada rol (números rápidos del negocio).
 */
@Named("panelBean")
@RequestScoped
public class PanelBean {

    @EJB
    private MPlatillo mPlatillo;

    @EJB
    private MUsuario mUsuario;

    @Inject
    private SesionBean sesion;

    /** Viene de ?acceso=denegado cuando el filtro rechazó una vista. */
    private String acceso;

    public boolean isAccesoDenegado() {
        return "denegado".equals(acceso);
    }

    public long getTotalPlatillos() {
        return sesion.getEstablecimientoId() == null ? 0 : mPlatillo.contar(sesion.getEstablecimientoId());
    }

    public long getPlatillosDisponibles() {
        return sesion.getEstablecimientoId() == null ? 0 : mPlatillo.contarDisponibles(sesion.getEstablecimientoId());
    }

    public long getPlatillosAgotados() {
        return getTotalPlatillos() - getPlatillosDisponibles();
    }

    public long getPersonalActivo() {
        return sesion.getEstablecimientoId() == null ? 0 : mUsuario.contarPersonalActivo(sesion.getEstablecimientoId());
    }

    /** Dirección pública del menú del negocio (para el código QR). */
    public String getEnlaceMenu() {
        if (sesion.getEstablecimientoId() == null) {
            return "";
        }
        HttpServletRequest req = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        return req.getScheme() + "://" + req.getServerName() + ":" + req.getServerPort()
                + req.getContextPath() + "/vistas/menu.xhtml?e=" + sesion.getEstablecimientoId();
    }

    public String getAcceso() {
        return acceso;
    }

    public void setAcceso(String acceso) {
        this.acceso = acceso;
    }
}
