package logicaneg;

import java.io.Serializable;
import java.math.BigDecimal;

/** Una sucursal en la vista consolidada del dueño (HU-21): lo vendido en el periodo y cómo va operando ahora. */
public class ResumenSucursal implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer sucursalId;
    private String nombre;
    private String gerente;
    private FilaReporte ventas = new FilaReporte("v", "v");
    private boolean cajaAbierta;
    private BigDecimal efectivoEsperado;
    private long comandasEnCocina;
    private long porCobrar;
    private long apartadosActivos;
    private long platillosDisponibles;
    private long platillosAgotados;

    public Integer getSucursalId() {
        return sucursalId;
    }

    public void setSucursalId(Integer sucursalId) {
        this.sucursalId = sucursalId;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getGerente() {
        return gerente;
    }

    public void setGerente(String gerente) {
        this.gerente = gerente;
    }

    public FilaReporte getVentas() {
        return ventas;
    }

    public void setVentas(FilaReporte ventas) {
        this.ventas = ventas;
    }

    public boolean isCajaAbierta() {
        return cajaAbierta;
    }

    public void setCajaAbierta(boolean cajaAbierta) {
        this.cajaAbierta = cajaAbierta;
    }

    public BigDecimal getEfectivoEsperado() {
        return efectivoEsperado;
    }

    public void setEfectivoEsperado(BigDecimal efectivoEsperado) {
        this.efectivoEsperado = efectivoEsperado;
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

    public long getPlatillosDisponibles() {
        return platillosDisponibles;
    }

    public void setPlatillosDisponibles(long platillosDisponibles) {
        this.platillosDisponibles = platillosDisponibles;
    }

    public long getPlatillosAgotados() {
        return platillosAgotados;
    }

    public void setPlatillosAgotados(long platillosAgotados) {
        this.platillosAgotados = platillosAgotados;
    }
}
