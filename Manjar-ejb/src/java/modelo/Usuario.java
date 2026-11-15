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
@Table(name = "usuario")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Usuario.findAll", query = "SELECT u FROM Usuario u"),
    @NamedQuery(name = "Usuario.findById", query = "SELECT u FROM Usuario u WHERE u.id = :id"),
    @NamedQuery(name = "Usuario.findByNombre", query = "SELECT u FROM Usuario u WHERE u.nombre = :nombre"),
    @NamedQuery(name = "Usuario.findByCorreo", query = "SELECT u FROM Usuario u WHERE u.correo = :correo"),
    @NamedQuery(name = "Usuario.findByContrasenahash", query = "SELECT u FROM Usuario u WHERE u.contrasenahash = :contrasenahash"),
    @NamedQuery(name = "Usuario.findByRol", query = "SELECT u FROM Usuario u WHERE u.rol = :rol"),
    @NamedQuery(name = "Usuario.findByEstado", query = "SELECT u FROM Usuario u WHERE u.estado = :estado")})
public class Usuario implements Serializable {

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
    @Column(name = "Correo")
    private String correo;
    @Size(max = 255)
    @Column(name = "Contrasenahash")
    private String contrasenahash;
    @Column(name = "Rol")
    private Integer rol;
    @Column(name = "Estado")
    private Integer estado;
    @OneToMany(mappedBy = "duenoid")
    private List<Establecimiento> establecimientoList;
    @OneToMany(mappedBy = "gerenteid")
    private List<Establecimiento> establecimientoList1;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "clienteid")
    private List<Apartado> apartadoList;
    @JoinColumn(name = "ESTABLECIMIENTOID", referencedColumnName = "ID")
    @ManyToOne
    private Establecimiento establecimientoid;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "usuarioid")
    private List<Caja> cajaList;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "meseroid")
    private List<Comanda> comandaList;

    public Usuario() {
    }

    public Usuario(Integer id) {
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

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getContrasenahash() {
        return contrasenahash;
    }

    public void setContrasenahash(String contrasenahash) {
        this.contrasenahash = contrasenahash;
    }

    public Integer getRol() {
        return rol;
    }

    public void setRol(Integer rol) {
        this.rol = rol;
    }

    public Integer getEstado() {
        return estado;
    }

    public void setEstado(Integer estado) {
        this.estado = estado;
    }

    @XmlTransient
    public List<Establecimiento> getEstablecimientoList() {
        return establecimientoList;
    }

    public void setEstablecimientoList(List<Establecimiento> establecimientoList) {
        this.establecimientoList = establecimientoList;
    }

    @XmlTransient
    public List<Establecimiento> getEstablecimientoList1() {
        return establecimientoList1;
    }

    public void setEstablecimientoList1(List<Establecimiento> establecimientoList1) {
        this.establecimientoList1 = establecimientoList1;
    }

    @XmlTransient
    public List<Apartado> getApartadoList() {
        return apartadoList;
    }

    public void setApartadoList(List<Apartado> apartadoList) {
        this.apartadoList = apartadoList;
    }

    public Establecimiento getEstablecimientoid() {
        return establecimientoid;
    }

    public void setEstablecimientoid(Establecimiento establecimientoid) {
        this.establecimientoid = establecimientoid;
    }

    @XmlTransient
    public List<Caja> getCajaList() {
        return cajaList;
    }

    public void setCajaList(List<Caja> cajaList) {
        this.cajaList = cajaList;
    }

    @XmlTransient
    public List<Comanda> getComandaList() {
        return comandaList;
    }

    public void setComandaList(List<Comanda> comandaList) {
        this.comandaList = comandaList;
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
        if (!(object instanceof Usuario)) {
            return false;
        }
        Usuario other = (Usuario) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "modelo.Usuario[ id=" + id + " ]";
    }
    
}
