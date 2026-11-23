package logicaneg;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/** Resumen de todas las sucursales del dueño en una sola pantalla (HU-21). */
public class ConsolidadoVista implements Serializable {

    private static final long serialVersionUID = 1L;

    private Date desde;
    private Date hasta;
    private List<ResumenSucursal> sucursales = new ArrayList<>();
    private FilaReporte total = new FilaReporte("total", "Total");
    private long cajasAbiertas;
    private long comandasEnCocina;
    private long porCobrar;
    private long apartadosActivos;

    /** La sucursal con más ingresos en el periodo, o null si no hubo ventas. */
    public ResumenSucursal getMayorVenta() {
        ResumenSucursal mejor = null;
        for (ResumenSucursal s : sucursales) {
            if (s.getVentas().getVentas() > 0
                    && (mejor == null || s.getVentas().getIngresos().compareTo(mejor.getVentas().getIngresos()) > 0)) {
                mejor = s;
            }
        }
        return mejor;
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

    public List<ResumenSucursal> getSucursales() {
        return sucursales;
    }

    public FilaReporte getTotal() {
        return total;
    }

    public long getCajasAbiertas() {
        return cajasAbiertas;
    }

    public void setCajasAbiertas(long cajasAbiertas) {
        this.cajasAbiertas = cajasAbiertas;
    }

    public long getComandasEnCocina() {
        return comandasEnCocina;
    }

    public void setComandasEnCocina(long comandasEnCocina) {
        this.comandasEnCocina = comandasEnCocina;
    }

    public long getPorCobrar() {
        return porCobrar;
    }

    public void setPorCobrar(long porCobrar) {
        this.porCobrar = porCobrar;
    }

    public long getApartadosActivos() {
        return apartadosActivos;
    }

    public void setApartadosActivos(long apartadosActivos) {
        this.apartadosActivos = apartadosActivos;
    }
}
