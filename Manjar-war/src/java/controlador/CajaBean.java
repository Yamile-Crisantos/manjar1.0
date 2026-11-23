package controlador;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import logicaneg.CajaVista;
import logicaneg.ComandaVista;
import logicaneg.Constantes;
import logicaneg.MCaja;
import logicaneg.MovimientoVista;
import logicaneg.ReglaNegocioException;

/**
 * Pantalla de caja del Dueño y del Gerente: HU-11 (abrir con monto inicial, cobrar, registrar retiros y
 * gastos) y HU-12 (corte y arqueo al final del día).
 *
 * La sucursal y el usuario salen siempre de la sesión y MCaja los vuelve a comprobar. Las listas se
 * guardan en la vista y se renuevan después de cada acción, para que el botón de una fila siempre
 * corresponda a la fila que se ve en pantalla.
 */
@Named("cajaBean")
@ViewScoped
public class CajaBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private MCaja mCaja;

    @Inject
    private SesionBean sesion;

    private CajaVista caja;
    private List<MovimientoVista> movimientos = new ArrayList<>();
    private List<ComandaVista> porCobrar = new ArrayList<>();
    private List<CajaVista> historial = new ArrayList<>();

    // Apertura
    private BigDecimal montoInicial;

    // Retiros y gastos
    private String claseEgreso = Constantes.CLASE_RETIRO;
    private BigDecimal montoEgreso;
    private String motivo;

    // Corte (dos pasos)
    private BigDecimal contado;
    private CajaVista vistaPrevia;
    private CajaVista ultimoCorte;

    // Detalle de una caja anterior
    private CajaVista detalle;
    private List<MovimientoVista> movimientosDetalle = new ArrayList<>();

    @PostConstruct
    public void init() {
        refrescar();
    }

    /** Vuelve a leer la caja, sus movimientos, las cuentas por cobrar y el historial. */
    public void refrescar() {
        Integer est = sesion.getEstablecimientoId();
        Integer usr = sesion.getUsuarioId();
        try {
            caja = mCaja.abierta(est, usr);
            movimientos = caja == null ? new ArrayList<MovimientoVista>() : mCaja.movimientos(est, usr, caja.getId());
            porCobrar = caja == null ? new ArrayList<ComandaVista>() : mCaja.porCobrar(est, usr);
            historial = mCaja.historial(est, usr, 15);
        } catch (ReglaNegocioException e) {
            Mensajes.error(e.getMessage());
        }
        if (caja == null) {
            vistaPrevia = null;
        }
    }

    // ------------------------------------------------------------------
    // Apertura
    // ------------------------------------------------------------------
    public void abrir() {
        try {
            CajaVista c = mCaja.abrir(sesion.getEstablecimientoId(), sesion.getUsuarioId(), montoInicial);
            Mensajes.info("Caja abierta con $" + c.getMontoInicial().setScale(2, RoundingMode.HALF_UP).toPlainString()
                    + ". Las porciones del día se reiniciaron.");
            montoInicial = null;
            ultimoCorte = null;
            detalle = null;
        } catch (ReglaNegocioException e) {
            Mensajes.error(e.getMessage());
        }
        refrescar();
    }

    // ------------------------------------------------------------------
    // Cobro y movimientos
    // ------------------------------------------------------------------
    public void cobrar(Integer comandaId, String metodo) {
        try {
            MovimientoVista m = mCaja.cobrar(sesion.getEstablecimientoId(), sesion.getUsuarioId(), comandaId, metodo);
            Mensajes.info("Cobrado $" + m.getMonto().setScale(2, RoundingMode.HALF_UP).toPlainString() + " (" + m.getMetodoTexto().toLowerCase() + ").");
            vistaPrevia = null;
        } catch (ReglaNegocioException e) {
            Mensajes.error(e.getMessage());
        }
        refrescar();
    }

    public void registrarEgreso() {
        try {
            MovimientoVista m = mCaja.registrarEgreso(sesion.getEstablecimientoId(), sesion.getUsuarioId(),
                    claseEgreso, montoEgreso, motivo);
            Mensajes.info((Constantes.CLASE_RETIRO.equals(m.getClase()) ? "Retiro" : "Gasto") + " registrado por $"
                    + m.getMonto().setScale(2, RoundingMode.HALF_UP).toPlainString() + ".");
            montoEgreso = null;
            motivo = null;
            vistaPrevia = null;
        } catch (ReglaNegocioException e) {
            Mensajes.error(e.getMessage());
        }
        refrescar();
    }

    // ------------------------------------------------------------------
    // Corte y arqueo
    // ------------------------------------------------------------------
    /** Paso 1: calcula el corte con el dinero contado, sin cerrar. */
    public void calcularCorte() {
        try {
            vistaPrevia = mCaja.vistaPreviaCorte(sesion.getEstablecimientoId(), sesion.getUsuarioId(), contado);
        } catch (ReglaNegocioException e) {
            vistaPrevia = null;
            Mensajes.error(e.getMessage());
        }
    }

    /** Vuelve a corregir el dinero contado. */
    public void corregirConteo() {
        vistaPrevia = null;
    }

    /** Paso 2: confirma y cierra la caja. */
    public void confirmarCorte() {
        if (vistaPrevia == null) {
            Mensajes.aviso("Primero calcula el corte.");
            return;
        }
        try {
            ultimoCorte = mCaja.cerrar(sesion.getEstablecimientoId(), sesion.getUsuarioId(), vistaPrevia.getId(),
                    vistaPrevia.getMontoContado(), vistaPrevia.getMontoEsperado());
            Mensajes.info("Caja cerrada. " + ultimoCorte.getResultadoTexto() + ".");
            vistaPrevia = null;
            contado = null;
            detalle = null;
        } catch (ReglaNegocioException e) {
            Mensajes.error(e.getMessage());
            // Si cambió el efectivo esperado, se enseña el corte actualizado para volver a confirmarlo
            calcularCorteSilencioso();
        }
        refrescar();
    }

    private void calcularCorteSilencioso() {
        try {
            vistaPrevia = mCaja.vistaPreviaCorte(sesion.getEstablecimientoId(), sesion.getUsuarioId(),
                    vistaPrevia == null ? contado : vistaPrevia.getMontoContado());
        } catch (ReglaNegocioException e) {
            vistaPrevia = null;
        }
    }

    // ------------------------------------------------------------------
    // Cajas anteriores
    // ------------------------------------------------------------------
    public void verDetalle(Integer cajaId) {
        try {
            detalle = mCaja.buscar(sesion.getEstablecimientoId(), sesion.getUsuarioId(), cajaId);
            movimientosDetalle = mCaja.movimientos(sesion.getEstablecimientoId(), sesion.getUsuarioId(), cajaId);
        } catch (ReglaNegocioException e) {
            detalle = null;
            Mensajes.error(e.getMessage());
        }
    }

    public void cerrarDetalle() {
        detalle = null;
        movimientosDetalle = new ArrayList<>();
    }

    // ------------------------------------------------------------------
    // Para la vista
    // ------------------------------------------------------------------
    public boolean isAbierta() {
        return caja != null;
    }

    public CajaVista getCaja() {
        return caja;
    }

    public List<MovimientoVista> getMovimientos() {
        return movimientos;
    }

    public List<ComandaVista> getPorCobrar() {
        return porCobrar;
    }

    public List<CajaVista> getHistorial() {
        return historial;
    }

    public BigDecimal getMontoInicial() {
        return montoInicial;
    }

    public void setMontoInicial(BigDecimal montoInicial) {
        this.montoInicial = montoInicial;
    }

    public String getClaseEgreso() {
        return claseEgreso;
    }

    public void setClaseEgreso(String claseEgreso) {
        this.claseEgreso = claseEgreso;
    }

    public BigDecimal getMontoEgreso() {
        return montoEgreso;
    }

    public void setMontoEgreso(BigDecimal montoEgreso) {
        this.montoEgreso = montoEgreso;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public BigDecimal getContado() {
        return contado;
    }

    public void setContado(BigDecimal contado) {
        this.contado = contado;
    }

    public CajaVista getVistaPrevia() {
        return vistaPrevia;
    }

    public CajaVista getUltimoCorte() {
        return ultimoCorte;
    }

    public CajaVista getDetalle() {
        return detalle;
    }

    public List<MovimientoVista> getMovimientosDetalle() {
        return movimientosDetalle;
    }
}
