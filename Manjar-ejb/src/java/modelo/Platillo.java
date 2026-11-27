/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

import java.io.Serializable;
import java.math.BigDecimal;
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
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author yamil
 */
@Entity
@Table(name = "platillo")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Platillo.findAll", query = "SELECT p FROM Platillo p"),
    @NamedQuery(name = "Platillo.findById", query = "SELECT p FROM Platillo p WHERE p.id = :id"),
    @NamedQuery(name = "Platillo.findByNombre", query = "SELECT p FROM Platillo p WHERE p.nombre = :nombre"),
    @NamedQuery(name = "Platillo.findByDescripcion", query = "SELECT p FROM Platillo p WHERE p.descripcion = :descripcion"),
    @NamedQuery(name = "Platillo.findByPrecio", query = "SELECT p FROM Platillo p WHERE p.precio = :precio"),
    @NamedQuery(name = "Platillo.findByCantidadEstimada", query = "SELECT p FROM Platillo p WHERE p.cantidadEstimada = :cantidadEstimada"),
    @NamedQuery(name = "Platillo.findByCantidadRestante", query = "SELECT p FROM Platillo p WHERE p.cantidadRestante = :cantidadRestante"),
    @NamedQuery(name = "Platillo.findByEstado", query = "SELECT p FROM Platillo p WHERE p.estado = :estado"),
    @NamedQuery(name = "Platillo.findByCosto", query = "SELECT p FROM Platillo p WHERE p.costo = :costo")})
public class Platillo implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "ID")
    private Integer id;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "Nombre")
    private String nombre;
    @Size(max = 255)
    @Column(name = "Descripcion")
    private String descripcion;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Basic(optional = false)
    @NotNull
    @Column(name = "Precio")
    private BigDecimal precio;
    @Column(name = "CantidadEstimada")
    private Integer cantidadEstimada;
    @Column(name = "CantidadRestante")
    private Integer cantidadRestante;
    @Column(name = "Estado")
    private Integer estado;
    @Column(name = "COSTO")
    private BigDecimal costo;
    /** Foto del platillo: un enlace http/https o la ruta interna "imagen/platillo/<id>?v=<n>" de una foto subida. */
    @Size(max = 500)
    @Column(name = "IMAGENURL")
    private String imagenurl;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "platilloid")
    private List<Detalleapartado> detalleapartadoList;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "platilloid")
    private List<Detallecomanda> detallecomandaList;
    @JoinColumn(name = "CATEGORIAID", referencedColumnName = "ID")
    @ManyToOne(optional = false)
    private Categoria categoriaid;
    @JoinColumn(name = "ESTABLECIMIENTOID", referencedColumnName = "ID")
    @ManyToOne(optional = false)
    private Establecimiento establecimientoid;

    public Platillo() {
    }

    public Platillo(Integer id) {
        this.id = id;
    }

    public Platillo(Integer id, String nombre, BigDecimal precio) {
        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
    }

    public String getImagenurl() {
        return imagenurl;
    }

    public void setImagenurl(String imagenurl) {
        this.imagenurl = imagenurl;
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

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public Integer getCantidadEstimada() {
        return cantidadEstimada;
    }

    public void setCantidadEstimada(Integer cantidadEstimada) {
        this.cantidadEstimada = cantidadEstimada;
    }

    public Integer getCantidadRestante() {
        return cantidadRestante;
    }

    public void setCantidadRestante(Integer cantidadRestante) {
        this.cantidadRestante = cantidadRestante;
    }

    public Integer getEstado() {
        return estado;
    }

    public void setEstado(Integer estado) {
        this.estado = estado;
    }

    public BigDecimal getCosto() {
        return costo;
    }

    public void setCosto(BigDecimal costo) {
        this.costo = costo;
    }

    @XmlTransient
    public List<Detalleapartado> getDetalleapartadoList() {
        return detalleapartadoList;
    }

    public void setDetalleapartadoList(List<Detalleapartado> detalleapartadoList) {
        this.detalleapartadoList = detalleapartadoList;
    }

    @XmlTransient
    public List<Detallecomanda> getDetallecomandaList() {
        return detallecomandaList;
    }

    public void setDetallecomandaList(List<Detallecomanda> detallecomandaList) {
        this.detallecomandaList = detallecomandaList;
    }

    public Categoria getCategoriaid() {
        return categoriaid;
    }

    public void setCategoriaid(Categoria categoriaid) {
        this.categoriaid = categoriaid;
    }

    public Establecimiento getEstablecimientoid() {
        return establecimientoid;
    }

    public void setEstablecimientoid(Establecimiento establecimientoid) {
        this.establecimientoid = establecimientoid;
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
        if (!(object instanceof Platillo)) {
            return false;
        }
        Platillo other = (Platillo) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "modelo.Platillo[ id=" + id + " ]";
    }
    
}
