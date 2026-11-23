package logicaneg;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;

/** Una fila de un reporte de ventas (un día, un platillo, un mesero o una sucursal). */
public class FilaReporte implements Serializable {

    private static final long serialVersionUID = 1L;

    private String clave;
    private String etiqueta;
    private int ventas;
    private int porciones;
    private BigDecimal ingresos = BigDecimal.ZERO;
    private BigDecimal costo = BigDecimal.ZERO;

    public FilaReporte(String clave, String etiqueta) {
        this.clave = clave;
        this.etiqueta = etiqueta;
    }

    public BigDecimal getGanancia() {
        return ingresos.subtract(costo);
    }

    /** Ganancia entre ingresos, en por ciento con dos decimales; 0 si no hubo ingresos. */
    public BigDecimal getMargen() {
        if (ingresos.signum() == 0) {
            return BigDecimal.ZERO.setScale(2);
        }
        return getGanancia().multiply(BigDecimal.valueOf(100)).divide(ingresos, 2, RoundingMode.HALF_UP);
    }

    /** Ingresos entre número de ventas (ticket promedio). */
    public BigDecimal getTicketPromedio() {
        if (ventas == 0) {
            return BigDecimal.ZERO.setScale(2);
        }
        return ingresos.divide(BigDecimal.valueOf(ventas), 2, RoundingMode.HALF_UP);
    }

    void sumar(int cantidad, BigDecimal precio, BigDecimal costoUnitario) {
        porciones += cantidad;
        ingresos = ingresos.add(precio.multiply(BigDecimal.valueOf(cantidad)));
        if (costoUnitario != null) {
            costo = costo.add(costoUnitario.multiply(BigDecimal.valueOf(cantidad)));
        }
    }

    void contarVenta() {
        ventas++;
    }

    public String getClave() {
        return clave;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public int getVentas() {
        return ventas;
    }

    public int getPorciones() {
        return porciones;
    }

    public BigDecimal getIngresos() {
        return ingresos.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getCosto() {
        return costo.setScale(2, RoundingMode.HALF_UP);
    }
}
