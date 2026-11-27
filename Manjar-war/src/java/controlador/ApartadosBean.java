package controlador;

import java.io.Serializable;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import logicaneg.ApartadoVista;
import logicaneg.Constantes;
import logicaneg.MApartado;
import logicaneg.ReglaNegocioException;

/**
 * Pantalla del Gerente (y del Dueño) para los apartados de su sucursal: HU-15 (ver los pedidos apartados y
 * pagados antes de prepararlos), HU-16 (confirmar la entrega con el código del comprobante) y el aviso de
 * "pedido listo" de HU-17. También registra el pago del anticipo (HU-13).
 *
 * La sucursal y el usuario salen de la sesión y MApartado los vuelve a comprobar en cada acción.
 */
@Named("apartadosBean")
@ViewScoped
public class ApartadosBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private MApartado mApartado;

    @Inject
    private SesionBean sesion;

    private List<ApartadoVista> porPagar = new ArrayList<>();
    private List<ApartadoVista> porPreparar = new ArrayList<>();
    private List<ApartadoVista> listos = new ArrayList<>();
    private List<ApartadoVista> terminados = new ArrayList<>();

    /** Código que se escribe en cada apartado listo, para entregarlo (la llave es el id del apartado). */
    private final Map<Integer, String> codigos = new HashMap<>();
    /** Cómo paga el cliente el resto al entregar y su referencia (la llave es el id del apartado). */
    private final Map<Integer, String> metodosSaldo = new HashMap<>();
    private final Map<Integer, String> referenciasSaldo = new HashMap<>();

    // Registro de pago (ventana)
    private ApartadoVista seleccionado;
    private String metodo = Constantes.METODO_EFECTIVO;
    private String referencia;

    @PostConstruct
    public void init() {
        refrescar();
    }

    public void refrescar() {
        try {
            List<ApartadoVista> activos = mApartado.activos(sesion.getEstablecimientoId(), sesion.getUsuarioId());
            porPagar = new ArrayList<>();
            porPreparar = new ArrayList<>();
            listos = new ArrayList<>();
            for (ApartadoVista a : activos) {
                if (a.isPendiente()) {
                    porPagar.add(a);
                } else if (a.isPagado()) {
                    porPreparar.add(a);
                } else if (a.isListo()) {
                    listos.add(a);
                }
            }
            terminados = mApartado.terminadosHoy(sesion.getEstablecimientoId(), sesion.getUsuarioId());
        } catch (ReglaNegocioException e) {
            Mensajes.error(e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // Registrar el pago
    // ------------------------------------------------------------------
    public void abrirPago(ApartadoVista a) {
        seleccionado = a;
        metodo = Constantes.METODO_EFECTIVO;
        referencia = null;
    }

    public void registrarPago() {
        if (seleccionado == null) {
            return;
        }
        try {
            ApartadoVista a = mApartado.registrarPago(sesion.getEstablecimientoId(), sesion.getUsuarioId(),
                    seleccionado.getId(), metodo, referencia);
            Mensajes.info((a.isConSaldo() ? "Anticipo registrado: apartado " : "Pago registrado: apartado ") + a.getCodigoVisible()
                    + ", $" + a.getAnticipo().setScale(2, RoundingMode.HALF_UP).toPlainString()
                    + (a.isConSaldo() ? ". El resto ($" + a.getSaldo().setScale(2, RoundingMode.HALF_UP).toPlainString()
                            + ") se cobra al entregar." : "."));
            seleccionado = null;
        } catch (ReglaNegocioException e) {
            Mensajes.error(e.getMessage());
            // Si el apartado ya no se puede pagar (venció, se canceló), se cierra la ventana para no dejarla con datos viejos
            if (e.getMessage().startsWith("Este apartado")) {
                seleccionado = null;
            }
        }
        refrescar();
    }

    public void cancelarPago() {
        seleccionado = null;
    }

    // ------------------------------------------------------------------
    // Listo, entregar y cancelar
    // ------------------------------------------------------------------
    public void marcarListo(Integer apartadoId) {
        try {
            ApartadoVista a = mApartado.marcarListo(sesion.getEstablecimientoId(), sesion.getUsuarioId(), apartadoId);
            Mensajes.info("Apartado " + a.getCodigoVisible() + " listo. Se avisó al cliente.");
        } catch (ReglaNegocioException e) {
            Mensajes.error(e.getMessage());
        }
        refrescar();
    }

    public void entregar(Integer apartadoId) {
        try {
            String metodo = metodosSaldo.get(apartadoId);
            ApartadoVista a = mApartado.entregar(sesion.getEstablecimientoId(), sesion.getUsuarioId(), apartadoId,
                    codigos.get(apartadoId), metodo == null ? Constantes.METODO_EFECTIVO : metodo, referenciasSaldo.get(apartadoId));
            Mensajes.info("Apartado " + a.getCodigoVisible() + " entregado"
                    + (a.isConSaldo() ? "; se cobró el resto ($" + a.getSaldo().setScale(2, RoundingMode.HALF_UP).toPlainString() + ")." : "."));
            codigos.remove(apartadoId);
            metodosSaldo.remove(apartadoId);
            referenciasSaldo.remove(apartadoId);
        } catch (ReglaNegocioException e) {
            Mensajes.error(e.getMessage());
        }
        refrescar();
    }

    public void cancelar(Integer apartadoId) {
        try {
            ApartadoVista a = mApartado.cancelarComoNegocio(sesion.getEstablecimientoId(), sesion.getUsuarioId(), apartadoId);
            Mensajes.info("Apartado " + a.getCodigoVisible() + " cancelado. Las porciones regresaron al menú.");
        } catch (ReglaNegocioException e) {
            Mensajes.error(e.getMessage());
        }
        refrescar();
    }

    // ------------------------------------------------------------------
    // Para la vista
    // ------------------------------------------------------------------
    public boolean isPagoConReferencia() {
        return Constantes.METODO_TRANSFERENCIA.equals(metodo) || Constantes.METODO_TARJETA.equals(metodo);
    }

    public List<ApartadoVista> getPorPagar() {
        return porPagar;
    }

    public List<ApartadoVista> getPorPreparar() {
        return porPreparar;
    }

    public List<ApartadoVista> getListos() {
        return listos;
    }

    public List<ApartadoVista> getTerminados() {
        return terminados;
    }

    public Map<Integer, String> getCodigos() {
        return codigos;
    }

    public Map<Integer, String> getMetodosSaldo() {
        return metodosSaldo;
    }

    public Map<Integer, String> getReferenciasSaldo() {
        return referenciasSaldo;
    }

    public int getPorcentajeAnticipo() {
        return MApartado.ANTICIPO_PCT;
    }

    public ApartadoVista getSeleccionado() {
        return seleccionado;
    }

    public String getMetodo() {
        return metodo;
    }

    public void setMetodo(String metodo) {
        this.metodo = metodo;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public int getMinutosRetencion() {
        return MApartado.RETENCION_MIN;
    }
}
