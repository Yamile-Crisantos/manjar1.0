package logicaneg;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Una caja (abierta o ya cerrada) con sus totales. Sirve también para la vista previa del corte:
 * en ese caso trae el monto contado y la diferencia sin haber cerrado todavía la caja (HU-11 y HU-12).
 *
 * Arqueo: el monto esperado es solo EFECTIVO = monto inicial + ingresos en efectivo - retiros - gastos.
 * Lo cobrado con tarjeta o transferencia se muestra aparte porque no está en el cajón.
 */
public class CajaVista implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;
    private String estado;
    private String abiertaPor;
    private Date fechaApertura;
    private Date fechaCierre;
    private BigDecimal montoInicial = BigDecimal.ZERO;
    private BigDecimal ingresosEfectivo = BigDecimal.ZERO;
    private BigDecimal ingresosTarjeta = BigDecimal.ZERO;
    private BigDecimal ingresosTransferencia = BigDecimal.ZERO;
    private BigDecimal retiros = BigDecimal.ZERO;
    private BigDecimal gastos = BigDecimal.ZERO;
    private BigDecimal montoEsperado = BigDecimal.ZERO;
    private BigDecimal montoContado;
    private BigDecimal diferencia;
    private int movimientos;
    private int pendientesDeCobro;

    public boolean isAbierta() {
        return Constantes.CAJA_ABIERTA.equals(estado);
    }

    /** Todo lo vendido y cobrado, sin importar cómo se pagó. */
    public BigDecimal getTotalVendido() {
        return ingresosEfectivo.add(ingresosTarjeta).add(ingresosTransferencia);
    }

    public BigDecimal getIngresosElectronicos() {
        return ingresosTarjeta.add(ingresosTransferencia);
    }

    public BigDecimal getTotalEgresos() {
        return retiros.add(gastos);
    }

    public boolean isHayContado() {
        return montoContado != null;
    }

    public boolean isCuadra() {
        return diferencia != null && diferencia.signum() == 0;
    }

    public boolean isSobrante() {
        return diferencia != null && diferencia.signum() > 0;
    }

    public boolean isFaltante() {
        return diferencia != null && diferencia.signum() < 0;
    }

    public String getResultadoTexto() {
        if (diferencia == null) {
            return "";
        }
        if (diferencia.signum() == 0) {
            return "La caja cuadra";
        }
        return diferencia.signum() > 0 ? "Sobra dinero" : "Falta dinero";
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getAbiertaPor() {
        return abiertaPor;
    }

    public void setAbiertaPor(String abiertaPor) {
        this.abiertaPor = abiertaPor;
    }

    public Date getFechaApertura() {
        return fechaApertura;
    }

    public void setFechaApertura(Date fechaApertura) {
        this.fechaApertura = fechaApertura;
    }

    public Date getFechaCierre() {
        return fechaCierre;
    }

    public void setFechaCierre(Date fechaCierre) {
        this.fechaCierre = fechaCierre;
    }

    public BigDecimal getMontoInicial() {
        return montoInicial;
    }

    public void setMontoInicial(BigDecimal montoInicial) {
        this.montoInicial = montoInicial == null ? BigDecimal.ZERO : montoInicial;
    }

    public BigDecimal getIngresosEfectivo() {
        return ingresosEfectivo;
    }

    public void setIngresosEfectivo(BigDecimal v) {
        this.ingresosEfectivo = v == null ? BigDecimal.ZERO : v;
    }

    public BigDecimal getIngresosTarjeta() {
        return ingresosTarjeta;
    }

    public void setIngresosTarjeta(BigDecimal v) {
        this.ingresosTarjeta = v == null ? BigDecimal.ZERO : v;
    }

    public BigDecimal getIngresosTransferencia() {
        return ingresosTransferencia;
    }

    public void setIngresosTransferencia(BigDecimal v) {
        this.ingresosTransferencia = v == null ? BigDecimal.ZERO : v;
    }

    public BigDecimal getRetiros() {
        return retiros;
    }

    public void setRetiros(BigDecimal v) {
        this.retiros = v == null ? BigDecimal.ZERO : v;
    }

    public BigDecimal getGastos() {
        return gastos;
    }

    public void setGastos(BigDecimal v) {
        this.gastos = v == null ? BigDecimal.ZERO : v;
    }

    public BigDecimal getMontoEsperado() {
        return montoEsperado;
    }

    public void setMontoEsperado(BigDecimal v) {
        this.montoEsperado = v == null ? BigDecimal.ZERO : v;
    }

    public BigDecimal getMontoContado() {
        return montoContado;
    }

    public void setMontoContado(BigDecimal montoContado) {
        this.montoContado = montoContado;
    }

    public BigDecimal getDiferencia() {
        return diferencia;
    }

    public void setDiferencia(BigDecimal diferencia) {
        this.diferencia = diferencia;
    }

    public int getMovimientos() {
        return movimientos;
    }

    public void setMovimientos(int movimientos) {
        this.movimientos = movimientos;
    }

    public int getPendientesDeCobro() {
        return pendientesDeCobro;
    }

    public void setPendientesDeCobro(int pendientesDeCobro) {
        this.pendientesDeCobro = pendientesDeCobro;
    }
}
