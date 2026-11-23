package controlador;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.Date;
import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import logicaneg.ConsolidadoVista;
import logicaneg.MReporte;
import logicaneg.ReglaNegocioException;

/**
 * Vista consolidada de todas las sucursales del dueño (HU-21): ventas del periodo y cómo va la operación ahora.
 * Solo el Dueño; el Gerente no tiene acceso (solo ve su sucursal, HU-19).
 */
@Named("consolidadoBean")
@ViewScoped
public class ConsolidadoBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private MReporte mReporte;

    @Inject
    private SesionBean sesion;

    private boolean iniciado;
    private String desde;
    private String hasta;
    private ConsolidadoVista datos;

    public void iniciar() {
        if (iniciado) {
            return;
        }
        iniciado = true;
        LocalDate hoy = LocalDate.now();
        desde = hoy.withDayOfMonth(1).toString();
        hasta = hoy.toString();
        generar();
    }

    public void generar() {
        datos = null;
        try {
            datos = mReporte.consolidado(sesion.getUsuarioId(), fecha(desde), fecha(hasta));
        } catch (ReglaNegocioException e) {
            Mensajes.error(e.getMessage());
        }
    }

    public void hoy() {
        desde = LocalDate.now().toString();
        hasta = desde;
        generar();
    }

    public void estaSemana() {
        LocalDate hoy = LocalDate.now();
        desde = hoy.with(DayOfWeek.MONDAY).toString();
        hasta = hoy.toString();
        generar();
    }

    public void esteMes() {
        LocalDate hoy = LocalDate.now();
        desde = hoy.withDayOfMonth(1).toString();
        hasta = hoy.toString();
        generar();
    }

    public void ultimos30() {
        LocalDate hoy = LocalDate.now();
        desde = hoy.minusDays(29).toString();
        hasta = hoy.toString();
        generar();
    }

    private Date fecha(String texto) throws ReglaNegocioException {
        if (texto == null || texto.trim().isEmpty()) {
            throw new ReglaNegocioException("Elige la fecha inicial y la fecha final del reporte.");
        }
        try {
            LocalDate d = LocalDate.parse(texto.trim());
            return Date.from(d.atStartOfDay(ZoneId.systemDefault()).toInstant());
        } catch (DateTimeParseException e) {
            throw new ReglaNegocioException("Elige una fecha válida (día, mes y año).");
        }
    }

    public ConsolidadoVista getDatos() {
        return datos;
    }

    public String getDesde() {
        return desde;
    }

    public void setDesde(String desde) {
        this.desde = desde;
    }

    public String getHasta() {
        return hasta;
    }

    public void setHasta(String hasta) {
        this.hasta = hasta;
    }
}
