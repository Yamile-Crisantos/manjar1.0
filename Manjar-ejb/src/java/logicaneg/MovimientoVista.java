package logicaneg;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/** Un movimiento de caja listo para mostrarse (HU-11). */
public class MovimientoVista implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;
    private String tipo;
    private String clase;
    private String metodo;
    private BigDecimal monto = BigDecimal.ZERO;
    private String concepto;
    private Date fechaHora;
    private String usuarioNombre;
    private Integer comandaId;
    private Integer apartadoId;

    public boolean isIngreso() {
        return Constantes.MOV_INGRESO.equals(tipo);
    }

    /** true si entró o salió efectivo de la caja (los pagos con tarjeta o transferencia no cuentan en el arqueo). */
    public boolean isEnEfectivo() {
        return metodo == null || Constantes.METODO_EFECTIVO.equals(metodo);
    }

    public String getClaseTexto() {
        if (Constantes.CLASE_VENTA.equals(clase)) {
            return "Venta";
        }
        if (Constantes.CLASE_APARTADO.equals(clase)) {
            return "Apartado";
        }
        if (Constantes.CLASE_RETIRO.equals(clase)) {
            return "Retiro";
        }
        if (Constantes.CLASE_GASTO.equals(clase)) {
            return "Gasto";
        }
        return isIngreso() ? "Ingreso" : "Egreso";
    }

    public String getMetodoTexto() {
        if (Constantes.METODO_TARJETA.equals(metodo)) {
            return "Tarjeta";
        }
        if (Constantes.METODO_TRANSFERENCIA.equals(metodo)) {
            return "Transferencia";
        }
        return "Efectivo";
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getClase() {
        return clase;
    }

    public void setClase(String clase) {
        this.clase = clase;
    }

    public String getMetodo() {
        return metodo;
    }

    public void setMetodo(String metodo) {
        this.metodo = metodo;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto == null ? BigDecimal.ZERO : monto;
    }

    public String getConcepto() {
        return concepto;
    }

    public void setConcepto(String concepto) {
        this.concepto = concepto;
    }

    public Date getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(Date fechaHora) {
        this.fechaHora = fechaHora;
    }

    public String getUsuarioNombre() {
        return usuarioNombre;
    }

    public void setUsuarioNombre(String usuarioNombre) {
        this.usuarioNombre = usuarioNombre;
    }

    public Integer getComandaId() {
        return comandaId;
    }

    public void setComandaId(Integer comandaId) {
        this.comandaId = comandaId;
    }

    public Integer getApartadoId() {
        return apartadoId;
    }

    public void setApartadoId(Integer apartadoId) {
        this.apartadoId = apartadoId;
    }
}
