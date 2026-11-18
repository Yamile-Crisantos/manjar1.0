package controlador;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import logicaneg.MEstablecimiento;
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

    @EJB
    private MEstablecimiento mEstablecimiento;

    @Inject
    private SesionBean sesion;

    /** Viene de ?acceso=denegado cuando el filtro rechazó una vista. */
    private String acceso;

    private Long totalPlatillos;
    private Long platillosDisponibles;

    public boolean isAccesoDenegado() {
        return "denegado".equals(acceso);
    }

    public long getTotalPlatillos() {
        if (totalPlatillos == null) {
            totalPlatillos = sesion.getEstablecimientoId() == null ? 0L : mPlatillo.contar(sesion.getEstablecimientoId());
        }
        return totalPlatillos;
    }

    public long getPlatillosDisponibles() {
        if (platillosDisponibles == null) {
            platillosDisponibles = sesion.getEstablecimientoId() == null
                    ? 0L : mPlatillo.contarDisponibles(sesion.getEstablecimientoId());
        }
        return platillosDisponibles;
    }

    public long getPlatillosAgotados() {
        return getTotalPlatillos() - getPlatillosDisponibles();
    }

    public long getPersonalActivo() {
        return sesion.getEstablecimientoId() == null ? 0 : mUsuario.contarPersonalActivo(sesion.getEstablecimientoId());
    }

    /** true si el dueño todavía no ha cargado ningún platillo: se le muestran los primeros pasos. */
    public boolean isNegocioSinMenu() {
        return sesion.isDueno() && sesion.getEstablecimientoId() != null && getTotalPlatillos() == 0;
    }

    /** Dirección pública del menú del negocio (la que lleva el código QR). */
    public String getEnlaceMenu() {
        if (sesion.getEstablecimientoId() == null) {
            return "";
        }
        String codigo = mEstablecimiento.codigoQR(sesion.getEstablecimientoId());
        return codigo == null ? "" : Enlaces.menuCompleto(codigo);
    }

    public String getAcceso() {
        return acceso;
    }

    public void setAcceso(String acceso) {
        this.acceso = acceso;
    }
}
