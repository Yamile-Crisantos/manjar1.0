package controlador;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.faces.model.SelectItem;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import logicaneg.MReporte;
import logicaneg.MSucursal;
import logicaneg.ReglaNegocioException;
import logicaneg.ReporteVista;
import logicaneg.SucursalVista;

/**
 * Reporte de ventas y rentabilidad de una sucursal (HU-20): por periodo, platillo y mesero. Solo el Dueño.
 * El dueño elige la sucursal; MReporte comprueba en cada consulta que sea suya.
 */
@Named("reportesBean")
@ViewScoped
public class ReportesBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private MReporte mReporte;

    @EJB
    private MSucursal mSucursal;

    @Inject
    private SesionBean sesion;

    // Parámetros opcionales de la dirección (los usa la vista consolidada para abrir el detalle de una sucursal)
    private String s;
    private String desdeParam;
    private String hastaParam;

    private boolean iniciado;
    private List<SucursalVista> sucursales = new ArrayList<>();
    private Map<Integer, String> platillos;
    private Map<Integer, String> meseros;

    private Integer sucursalId;
    private String desde;
    private String hasta;
    private String agrupar = MReporte.AGRUPAR_DIA;
    private Integer platilloId;
    private Integer meseroId;

    private ReporteVista reporte;

    /** Se ejecuta una vez, al abrir la pantalla (después de leer los parámetros de la dirección). */
    public void iniciar() {
        if (iniciado) {
            return;
        }
        iniciado = true;
        try {
            sucursales = mSucursal.listar(sesion.getUsuarioId());
        } catch (ReglaNegocioException e) {
            Mensajes.error(e.getMessage());
            return;
        }
        sucursalId = sucursalValida(s) != null ? sucursalValida(s) : sesion.getEstablecimientoId();
        LocalDate hoy = LocalDate.now();
        desde = esFecha(desdeParam) ? desdeParam : hoy.withDayOfMonth(1).toString();
        hasta = esFecha(hastaParam) ? hastaParam : hoy.toString();
        cargarOpciones();
        generar();
    }

    private Integer sucursalValida(String texto) {
        try {
            Integer id = texto == null ? null : Integer.valueOf(texto.trim());
            for (SucursalVista v : sucursales) {
                if (v.getId().equals(id)) {
                    return id;
                }
            }
        } catch (NumberFormatException e) {
            // se ignora: se usa la sucursal que el dueño está operando
        }
        return null;
    }

    private static boolean esFecha(String t) {
        try {
            LocalDate.parse(t);
            return true;
        } catch (DateTimeParseException | NullPointerException e) {
            return false;
        }
    }

    private void cargarOpciones() {
        try {
            platillos = mReporte.platillos(sucursalId, sesion.getUsuarioId());
            meseros = mReporte.meseros(sucursalId, sesion.getUsuarioId());
        } catch (ReglaNegocioException e) {
            Mensajes.error(e.getMessage());
            platillos = new java.util.LinkedHashMap<>();
            meseros = new java.util.LinkedHashMap<>();
        }
    }

    // ------------------------------------------------------------------
    // Acciones
    // ------------------------------------------------------------------
    public void generar() {
        reporte = null;
        try {
            reporte = mReporte.ventas(sucursalId, sesion.getUsuarioId(), fecha(desde), fecha(hasta), agrupar, platilloId, meseroId);
        } catch (ReglaNegocioException e) {
            Mensajes.error(e.getMessage());
        }
    }

    /** El dueño eligió otra sucursal: los filtros de platillo y mesero se limpian porque son de la anterior. */
    public void cambiarSucursal() {
        platilloId = null;
        meseroId = null;
        cargarOpciones();
        generar();
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

    public void limpiarFiltros() {
        platilloId = null;
        meseroId = null;
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

    // ------------------------------------------------------------------
    // Para la vista
    // ------------------------------------------------------------------
    public List<SelectItem> getOpcionesSucursal() {
        List<SelectItem> r = new ArrayList<>();
        for (SucursalVista v : sucursales) {
            r.add(new SelectItem(v.getId(), v.getNombre()));
        }
        return r;
    }

    public List<SelectItem> getOpcionesPlatillo() {
        return opciones(platillos);
    }

    public List<SelectItem> getOpcionesMesero() {
        return opciones(meseros);
    }

    private static List<SelectItem> opciones(Map<Integer, String> mapa) {
        List<SelectItem> r = new ArrayList<>();
        if (mapa != null) {
            for (Map.Entry<Integer, String> e : mapa.entrySet()) {
                r.add(new SelectItem(e.getKey(), e.getValue()));
            }
        }
        return r;
    }

    public boolean isVariasSucursales() {
        return sucursales.size() > 1;
    }

    public boolean isFiltrado() {
        return platilloId != null || meseroId != null;
    }

    public ReporteVista getReporte() {
        return reporte;
    }

    public Integer getSucursalId() {
        return sucursalId;
    }

    public void setSucursalId(Integer sucursalId) {
        this.sucursalId = sucursalId;
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

    public String getAgrupar() {
        return agrupar;
    }

    public void setAgrupar(String agrupar) {
        this.agrupar = agrupar;
    }

    public Integer getPlatilloId() {
        return platilloId;
    }

    public void setPlatilloId(Integer platilloId) {
        this.platilloId = platilloId;
    }

    public Integer getMeseroId() {
        return meseroId;
    }

    public void setMeseroId(Integer meseroId) {
        this.meseroId = meseroId;
    }

    public String getS() {
        return s;
    }

    public void setS(String s) {
        this.s = s;
    }

    public String getDesdeParam() {
        return desdeParam;
    }

    public void setDesdeParam(String desdeParam) {
        this.desdeParam = desdeParam;
    }

    public String getHastaParam() {
        return hastaParam;
    }

    public void setHastaParam(String hastaParam) {
        this.hastaParam = hastaParam;
    }
}
