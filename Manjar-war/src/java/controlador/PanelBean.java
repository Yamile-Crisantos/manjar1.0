package controlador;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import logicaneg.ApartadoVista;
import logicaneg.MApartado;
import logicaneg.MEstablecimiento;
import logicaneg.MPlatillo;
import logicaneg.MUsuario;
import logicaneg.ReglaNegocioException;

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

    @EJB
    private MApartado mApartado;

    @Inject
    private SesionBean sesion;

    /** Viene de ?acceso=denegado cuando el filtro rechazó una vista. */
    private String acceso;

    private Long totalPlatillos;
    private Long platillosDisponibles;
    private List<ApartadoVista> apartadosCliente;

    public boolean isAccesoDenegado() {
        return "denegado".equals(acceso);
    }

    /** Los conteos se piden con el usuario de la sesión: si ya no trabaja en esa sucursal el EJB los rechaza y se muestran en cero. */
    public long getTotalPlatillos() {
        if (totalPlatillos == null) {
            try {
                totalPlatillos = sesion.getEstablecimientoId() == null ? 0L
                        : mPlatillo.contar(sesion.getEstablecimientoId(), sesion.getUsuarioId());
            } catch (ReglaNegocioException e) {
                totalPlatillos = 0L;
            }
        }
        return totalPlatillos;
    }

    public long getPlatillosDisponibles() {
        if (platillosDisponibles == null) {
            try {
                platillosDisponibles = sesion.getEstablecimientoId() == null ? 0L
                        : mPlatillo.contarDisponibles(sesion.getEstablecimientoId(), sesion.getUsuarioId());
            } catch (ReglaNegocioException e) {
                platillosDisponibles = 0L;
            }
        }
        return platillosDisponibles;
    }

    public long getPlatillosAgotados() {
        return getTotalPlatillos() - getPlatillosDisponibles();
    }

    public long getPersonalActivo() {
        try {
            return sesion.getEstablecimientoId() == null ? 0 : mUsuario.contarPersonalActivo(sesion.getEstablecimientoId(), sesion.getUsuarioId());
        } catch (ReglaNegocioException e) {
            return 0;
        }
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

    // ------------------------------------------------------------------
    // Panel del cliente: lo que le interesa saber al entrar
    // ------------------------------------------------------------------
    private List<ApartadoVista> apartadosCliente() {
        if (apartadosCliente == null) {
            apartadosCliente = new java.util.ArrayList<>();
            if (sesion.isCliente()) {
                try {
                    apartadosCliente = mApartado.delCliente(sesion.getUsuarioId());
                } catch (ReglaNegocioException e) {
                    // sin datos: el panel se muestra sin el resumen
                }
            }
        }
        return apartadosCliente;
    }

    /** Apartados en curso (por pagar, pagados o listos). */
    public int getApartadosActivos() {
        int n = 0;
        for (ApartadoVista a : apartadosCliente()) {
            if (a.isActivo()) {
                n++;
            }
        }
        return n;
    }

    /** Apartados que ya se pueden recoger: el aviso más importante del panel. */
    public int getApartadosListos() {
        int n = 0;
        for (ApartadoVista a : apartadosCliente()) {
            if (a.isListo()) {
                n++;
            }
        }
        return n;
    }

    public String getAcceso() {
        return acceso;
    }

    public void setAcceso(String acceso) {
        this.acceso = acceso;
    }
}
