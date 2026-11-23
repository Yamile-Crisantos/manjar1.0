package logicaneg;

import java.io.Serializable;
import java.util.Date;

/** Un aviso para el cliente (HU-17). */
public class NotificacionVista implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;
    private Integer apartadoId;
    private String tipo;
    private String mensaje;
    private String negocioNombre;
    private Date fecha;
    private boolean leida;

    public boolean isListo() {
        return Constantes.NOTIF_LISTO.equals(tipo);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getApartadoId() {
        return apartadoId;
    }

    public void setApartadoId(Integer apartadoId) {
        this.apartadoId = apartadoId;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getNegocioNombre() {
        return negocioNombre;
    }

    public void setNegocioNombre(String negocioNombre) {
        this.negocioNombre = negocioNombre;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public boolean isLeida() {
        return leida;
    }

    public void setLeida(boolean leida) {
        this.leida = leida;
    }
}
