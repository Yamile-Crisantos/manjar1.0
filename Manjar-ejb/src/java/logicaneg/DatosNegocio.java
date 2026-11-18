package logicaneg;

import java.io.Serializable;
import modelo.Establecimiento;

/**
 * Lo que el dueño puede cambiar de su negocio en "Mi negocio": datos generales,
 * estilo del menú público, contacto y aviso del día.
 */
public class DatosNegocio implements Serializable {

    private static final long serialVersionUID = 1L;

    private String nombre;
    private String direccion;
    private String lema;
    private String color = Temas.PREDETERMINADO;
    private String diseno = Temas.DISENO_PREDETERMINADO;
    private String tipografia = Temas.TIPOGRAFIA_PREDETERMINADA;
    private String esquinas = Temas.ESQUINAS_PREDETERMINADAS;
    private String fondo = Temas.FONDO_PREDETERMINADO;
    private String telefono;
    private String whatsapp;
    private String horario;
    private String instagram;
    private String facebook;
    private String aviso;

    /** Copia los datos de un negocio; lo que falte (negocios anteriores) toma el valor predeterminado. */
    public static DatosNegocio de(Establecimiento e) {
        DatosNegocio d = new DatosNegocio();
        d.nombre = e.getNombre();
        d.direccion = e.getDireccion();
        d.lema = e.getLema();
        d.color = Temas.normalizar(e.getColorMenu());
        d.diseno = Temas.normalizarDiseno(e.getDiseno());
        d.tipografia = Temas.normalizarTipografia(e.getTipografia());
        d.esquinas = Temas.normalizarEsquinas(e.getEsquinas());
        d.fondo = Temas.normalizarFondo(e.getFondo());
        d.telefono = e.getTelefono();
        d.whatsapp = e.getWhatsapp();
        d.horario = e.getHorario();
        d.instagram = e.getInstagram();
        d.facebook = e.getFacebook();
        d.aviso = e.getAviso();
        return d;
    }

    /** Clases CSS del estilo elegido (para la vista previa). */
    public String getClases() {
        return Temas.clases(diseno, tipografia, esquinas, fondo);
    }

    /** Variables de color del tema elegido (para la vista previa). */
    public String getEstiloTema() {
        return Temas.estilo(color, fondo);
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

    public String getLema() {
        return lema;
    }

    public void setLema(String lema) {
        this.lema = lema;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getDiseno() {
        return diseno;
    }

    public void setDiseno(String diseno) {
        this.diseno = diseno;
    }

    public String getTipografia() {
        return tipografia;
    }

    public void setTipografia(String tipografia) {
        this.tipografia = tipografia;
    }

    public String getEsquinas() {
        return esquinas;
    }

    public void setEsquinas(String esquinas) {
        this.esquinas = esquinas;
    }

    public String getFondo() {
        return fondo;
    }

    public void setFondo(String fondo) {
        this.fondo = fondo;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getWhatsapp() {
        return whatsapp;
    }

    public void setWhatsapp(String whatsapp) {
        this.whatsapp = whatsapp;
    }

    public String getHorario() {
        return horario;
    }

    public void setHorario(String horario) {
        this.horario = horario;
    }

    public String getInstagram() {
        return instagram;
    }

    public void setInstagram(String instagram) {
        this.instagram = instagram;
    }

    public String getFacebook() {
        return facebook;
    }

    public void setFacebook(String facebook) {
        this.facebook = facebook;
    }

    public String getAviso() {
        return aviso;
    }

    public void setAviso(String aviso) {
        this.aviso = aviso;
    }
}
