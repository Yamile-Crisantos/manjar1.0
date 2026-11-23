package logicaneg;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/** Resultado del reporte de ventas y rentabilidad de una sucursal (HU-20). */
public class ReporteVista implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer sucursalId;
    private String sucursalNombre;
    private Date desde;
    private Date hasta;
    private String agrupar;
    private FilaReporte total = new FilaReporte("total", "Total");
    private List<FilaReporte> porPeriodo = new ArrayList<>();
    private List<FilaReporte> porPlatillo = new ArrayList<>();
    private List<FilaReporte> porMesero = new ArrayList<>();
    private List<String> platillosSinCosto = new ArrayList<>();

    public boolean isVacio() {
        return total.getVentas() == 0;
    }

    public boolean isHaySinCosto() {
        return !platillosSinCosto.isEmpty();
    }

    /** El platillo que más ganancia dejó (el primero de la lista), o null si no hubo ventas. */
    public FilaReporte getMejorPlatillo() {
        return porPlatillo.isEmpty() ? null : porPlatillo.get(0);
    }

    public Integer getSucursalId() {
        return sucursalId;
    }

    public void setSucursalId(Integer sucursalId) {
        this.sucursalId = sucursalId;
    }

    public String getSucursalNombre() {
        return sucursalNombre;
    }

    public void setSucursalNombre(String sucursalNombre) {
        this.sucursalNombre = sucursalNombre;
    }

    public Date getDesde() {
        return desde;
    }

    public void setDesde(Date desde) {
        this.desde = desde;
    }

    public Date getHasta() {
        return hasta;
    }

    public void setHasta(Date hasta) {
        this.hasta = hasta;
    }

    public String getAgrupar() {
        return agrupar;
    }

    public void setAgrupar(String agrupar) {
        this.agrupar = agrupar;
    }

    public FilaReporte getTotal() {
        return total;
    }

    public void setTotal(FilaReporte total) {
        this.total = total;
    }

    public List<FilaReporte> getPorPeriodo() {
        return porPeriodo;
    }

    public List<FilaReporte> getPorPlatillo() {
        return porPlatillo;
    }

    public List<FilaReporte> getPorMesero() {
        return porMesero;
    }

    public List<String> getPlatillosSinCosto() {
        return platillosSinCosto;
    }

    public BigDecimal getIngresos() {
        return total.getIngresos();
    }
}
