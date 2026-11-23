package controlador;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import logicaneg.MEstablecimiento;

/**
 * Datos de la página del código QR (HU-06): la ruta del menú público del negocio de la sesión.
 * El dibujo del QR lo hace el navegador (resources/js/qrcode.js).
 */
@Named("qrBean")
@RequestScoped
public class QrBean {

    @EJB
    private MEstablecimiento mEstablecimiento;

    @Inject
    private SesionBean sesion;

    private String codigo;
    private boolean cargado;

    public String getCodigo() {
        if (!cargado) {
            codigo = sesion.getEstablecimientoId() == null ? null : mEstablecimiento.codigoQR(sesion.getEstablecimientoId());
            cargado = true;
        }
        return codigo;
    }

    /** Ruta del menú en este servidor (el navegador le antepone su dirección para armar el enlace completo). */
    public String getRutaMenu() {
        return getCodigo() == null ? "" : Enlaces.rutaMenu(getCodigo());
    }

    /** Dirección completa del menú, para mostrarla como texto. */
    public String getEnlaceMenu() {
        return getCodigo() == null ? "" : Enlaces.menuCompleto(getCodigo());
    }
}
