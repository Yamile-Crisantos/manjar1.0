package logicaneg;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/** Un apartado listo para mostrarse al cliente o al gerente (HU-13, HU-15, HU-16, HU-17). */
public class ApartadoVista implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;
    private String codigo;
    private String estado;
    private Integer negocioId;
    private String negocioNombre;
    private String clienteNombre;
    private Date fechaCreacion;
    private Date horaRecoleccion;
    private Date venceEn;
    private BigDecimal total = BigDecimal.ZERO;
    private final List<ComandaVista.Linea> lineas = new ArrayList<>();
    private String metodoPago;
    private String referenciaPago;
    private Date fechaPago;
    private Date fechaEntrega;
    private String entregadoPor;
    private int minutosParaVencer;

    /** Código como se le muestra al cliente: K7M-2QX. */
    public String getCodigoVisible() {
        if (codigo == null || codigo.length() <= 3) {
            return codigo;
        }
        return codigo.substring(0, 3) + "-" + codigo.substring(3);
    }

    public boolean isPendiente() {
        return Constantes.APARTADO_PENDIENTE.equals(estado);
    }

    public boolean isPagado() {
        return Constantes.APARTADO_PAGADO.equals(estado);
    }

    public boolean isListo() {
        return Constantes.APARTADO_LISTO.equals(estado);
    }

    public boolean isEntregado() {
        return Constantes.APARTADO_ENTREGADO.equals(estado);
    }

    public boolean isCancelado() {
        return Constantes.APARTADO_CANCELADO.equals(estado);
    }

    public boolean isVencido() {
        return Constantes.APARTADO_VENCIDO.equals(estado);
    }

    /** true mientras el apartado sigue en proceso (aún no se entrega ni se cancela). */
    public boolean isActivo() {
        return isPendiente() || isPagado() || isListo();
    }

    public String getEstadoTexto() {
        if (isPendiente()) {
            return "Esperando pago";
        }
        if (isPagado()) {
            return "Pagado";
        }
        if (isListo()) {
            return "Listo para recoger";
        }
        if (isEntregado()) {
            return "Entregado";
        }
        if (isCancelado()) {
            return "Cancelado";
        }
        if (isVencido()) {
            return "Venció sin pago";
        }
        return estado;
    }

    public int getPorciones() {
        int n = 0;
        for (ComandaVista.Linea l : lineas) {
            n += l.getCantidad();
        }
        return n;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Integer getNegocioId() {
        return negocioId;
    }

    public void setNegocioId(Integer negocioId) {
        this.negocioId = negocioId;
    }

    public String getNegocioNombre() {
        return negocioNombre;
    }

    public void setNegocioNombre(String negocioNombre) {
        this.negocioNombre = negocioNombre;
    }

    public String getClienteNombre() {
        return clienteNombre;
    }

    public void setClienteNombre(String clienteNombre) {
        this.clienteNombre = clienteNombre;
    }

    public Date getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Date fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Date getHoraRecoleccion() {
        return horaRecoleccion;
    }

    public void setHoraRecoleccion(Date horaRecoleccion) {
        this.horaRecoleccion = horaRecoleccion;
    }

    public Date getVenceEn() {
        return venceEn;
    }

    public void setVenceEn(Date venceEn) {
        this.venceEn = venceEn;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total == null ? BigDecimal.ZERO : total;
    }

    public List<ComandaVista.Linea> getLineas() {
        return lineas;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public String getMetodoPagoTexto() {
        if (Constantes.METODO_TARJETA.equals(metodoPago)) {
            return "Tarjeta";
        }
        if (Constantes.METODO_TRANSFERENCIA.equals(metodoPago)) {
            return "Transferencia";
        }
        return metodoPago == null ? "" : "Efectivo";
    }

    public String getReferenciaPago() {
        return referenciaPago;
    }

    public void setReferenciaPago(String referenciaPago) {
        this.referenciaPago = referenciaPago;
    }

    public Date getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(Date fechaPago) {
        this.fechaPago = fechaPago;
    }

    public Date getFechaEntrega() {
        return fechaEntrega;
    }

    public void setFechaEntrega(Date fechaEntrega) {
        this.fechaEntrega = fechaEntrega;
    }

    public String getEntregadoPor() {
        return entregadoPor;
    }

    public void setEntregadoPor(String entregadoPor) {
        this.entregadoPor = entregadoPor;
    }

    public int getMinutosParaVencer() {
        return minutosParaVencer;
    }

    public void setMinutosParaVencer(int minutosParaVencer) {
        this.minutosParaVencer = minutosParaVencer;
    }
}
