package logicaneg;

import java.io.Serializable;

/** Una sucursal del dueño tal como se muestra en la pantalla de sucursales (HU-18). */
public class SucursalVista implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;
    private String nombre;
    private String direccion;
    private String codigoQR;
    private boolean principal;
    private Integer gerenteId;
    private String gerenteNombre;
    private String gerenteCorreo;
    private boolean gerenteActivo;
    private long meseros;

    /** true si la sucursal tiene un gerente activo; si no, el dueño la opera con su propia cuenta. */
    public boolean isConGerente() {
        return gerenteId != null && gerenteActivo;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getCodigoQR() {
        return codigoQR;
    }

    public void setCodigoQR(String codigoQR) {
        this.codigoQR = codigoQR;
    }

    public boolean isPrincipal() {
        return principal;
    }

    public void setPrincipal(boolean principal) {
        this.principal = principal;
    }

    public Integer getGerenteId() {
        return gerenteId;
    }

    public void setGerenteId(Integer gerenteId) {
        this.gerenteId = gerenteId;
    }

    public String getGerenteNombre() {
        return gerenteNombre;
    }

    public void setGerenteNombre(String gerenteNombre) {
        this.gerenteNombre = gerenteNombre;
    }

    public String getGerenteCorreo() {
        return gerenteCorreo;
    }

    public void setGerenteCorreo(String gerenteCorreo) {
        this.gerenteCorreo = gerenteCorreo;
    }

    public boolean isGerenteActivo() {
        return gerenteActivo;
    }

    public void setGerenteActivo(boolean gerenteActivo) {
        this.gerenteActivo = gerenteActivo;
    }

    public long getMeseros() {
        return meseros;
    }

    public void setMeseros(long meseros) {
        this.meseros = meseros;
    }
}
