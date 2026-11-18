/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

import java.io.Serializable;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author yamil
 */
@Entity
@Table(name = "establecimiento")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Establecimiento.findAll", query = "SELECT e FROM Establecimiento e"),
    @NamedQuery(name = "Establecimiento.findById", query = "SELECT e FROM Establecimiento e WHERE e.id = :id"),
    @NamedQuery(name = "Establecimiento.findByNombre", query = "SELECT e FROM Establecimiento e WHERE e.nombre = :nombre"),
    @NamedQuery(name = "Establecimiento.findByTenantId", query = "SELECT e FROM Establecimiento e WHERE e.tenantId = :tenantId"),
    @NamedQuery(name = "Establecimiento.findBySystemId", query = "SELECT e FROM Establecimiento e WHERE e.systemId = :systemId"),
    @NamedQuery(name = "Establecimiento.findByCodigoQR", query = "SELECT e FROM Establecimiento e WHERE e.codigoQR = :codigoQR"),
    @NamedQuery(name = "Establecimiento.findByDireccion", query = "SELECT e FROM Establecimiento e WHERE e.direccion = :direccion")})
public class Establecimiento implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "ID")
    private Integer id;
    @Size(max = 255)
    @Column(name = "Nombre")
    private String nombre;
    @Size(max = 255)
    @Column(name = "TenantId")
    private String tenantId;
    @Size(max = 255)
    @Column(name = "SystemId")
    private String systemId;
    @Size(max = 255)
    @Column(name = "CodigoQR")
    private String codigoQR;
    @Size(max = 255)
    @Column(name = "DIRECCION")
    private String direccion;
    /** Color de acento del menú público (#RRGGBB de la paleta de Temas). Personalización del negocio. */
    @Size(max = 7)
    @Column(name = "ColorMenu")
    private String colorMenu;
    /** Lema o frase corta que aparece bajo el nombre en el menú público. */
    @Size(max = 120)
    @Column(name = "Lema")
    private String lema;
    /** Diseño de la lista de platillos del menú público: "tarjetas" o "lista". */
    @Size(max = 10)
    @Column(name = "DisenoMenu")
    private String diseno;
    /** Tipografía del menú público: "clasica", "moderna" o "calida". */
    @Size(max = 10)
    @Column(name = "Tipografia")
    private String tipografia;
    /** Forma de las esquinas del menú público: "redondas" o "rectas". */
    @Size(max = 10)
    @Column(name = "Esquinas")
    private String esquinas;
    /** Fondo del menú público: "claro" u "oscuro". */
    @Size(max = 10)
    @Column(name = "FondoMenu")
    private String fondo;
    /** Teléfono del negocio (se muestra en el menú público). */
    @Size(max = 20)
    @Column(name = "Telefono")
    private String telefono;
    /** WhatsApp del negocio (se muestra en el menú público). */
    @Size(max = 20)
    @Column(name = "Whatsapp")
    private String whatsapp;
    /** Horario de atención, en texto libre. */
    @Size(max = 255)
    @Column(name = "Horario")
    private String horario;
    /** Usuario de Instagram, sin @. */
    @Size(max = 30)
    @Column(name = "Instagram")
    private String instagram;
    /** Nombre de la página de Facebook (lo que va después de facebook.com/). */
    @Size(max = 50)
    @Column(name = "Facebook")
    private String facebook;
    /** Aviso del día que se destaca arriba del menú. */
    @Size(max = 160)
    @Column(name = "Aviso")
    private String aviso;
    @JoinColumn(name = "DUENOID", referencedColumnName = "ID")
    @ManyToOne
    private Usuario duenoid;
    @JoinColumn(name = "GERENTEID", referencedColumnName = "ID")
    @ManyToOne
    private Usuario gerenteid;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "establecimientoid")
    private List<Apartado> apartadoList;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "establecimientoid")
    private List<Categoria> categoriaList;
    @OneToMany(mappedBy = "establecimientoid")
    private List<Usuario> usuarioList;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "establecimientoid")
    private List<Caja> cajaList;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "establecimientoid")
    private List<Platillo> platilloList;

    public Establecimiento() {
    }

    public Establecimiento(Integer id) {
        this.id = id;
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

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getSystemId() {
        return systemId;
    }

    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }

    public String getCodigoQR() {
        return codigoQR;
    }

    public void setCodigoQR(String codigoQR) {
        this.codigoQR = codigoQR;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getColorMenu() {
        return colorMenu;
    }

    public void setColorMenu(String colorMenu) {
        this.colorMenu = colorMenu;
    }

    public String getLema() {
        return lema;
    }

    public void setLema(String lema) {
        this.lema = lema;
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

    public Usuario getDuenoid() {
        return duenoid;
    }

    public void setDuenoid(Usuario duenoid) {
        this.duenoid = duenoid;
    }

    public Usuario getGerenteid() {
        return gerenteid;
    }

    public void setGerenteid(Usuario gerenteid) {
        this.gerenteid = gerenteid;
    }

    @XmlTransient
    public List<Apartado> getApartadoList() {
        return apartadoList;
    }

    public void setApartadoList(List<Apartado> apartadoList) {
        this.apartadoList = apartadoList;
    }

    @XmlTransient
    public List<Categoria> getCategoriaList() {
        return categoriaList;
    }

    public void setCategoriaList(List<Categoria> categoriaList) {
        this.categoriaList = categoriaList;
    }

    @XmlTransient
    public List<Usuario> getUsuarioList() {
        return usuarioList;
    }

    public void setUsuarioList(List<Usuario> usuarioList) {
        this.usuarioList = usuarioList;
    }

    @XmlTransient
    public List<Caja> getCajaList() {
        return cajaList;
    }

    public void setCajaList(List<Caja> cajaList) {
        this.cajaList = cajaList;
    }

    @XmlTransient
    public List<Platillo> getPlatilloList() {
        return platilloList;
    }

    public void setPlatilloList(List<Platillo> platilloList) {
        this.platilloList = platilloList;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Establecimiento)) {
            return false;
        }
        Establecimiento other = (Establecimiento) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "modelo.Establecimiento[ id=" + id + " ]";
    }
    
}
