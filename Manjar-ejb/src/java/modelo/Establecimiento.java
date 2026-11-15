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
