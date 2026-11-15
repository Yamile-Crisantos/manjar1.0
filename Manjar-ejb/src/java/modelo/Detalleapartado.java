/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

import java.io.Serializable;
import java.math.BigDecimal;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author yamil
 */
@Entity
@Table(name = "detalleapartado")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Detalleapartado.findAll", query = "SELECT d FROM Detalleapartado d"),
    @NamedQuery(name = "Detalleapartado.findById", query = "SELECT d FROM Detalleapartado d WHERE d.id = :id"),
    @NamedQuery(name = "Detalleapartado.findByCantidad", query = "SELECT d FROM Detalleapartado d WHERE d.cantidad = :cantidad"),
    @NamedQuery(name = "Detalleapartado.findByPreciounitario", query = "SELECT d FROM Detalleapartado d WHERE d.preciounitario = :preciounitario")})
public class Detalleapartado implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "ID")
    private Integer id;
    @Basic(optional = false)
    @NotNull
    @Column(name = "CANTIDAD")
    private int cantidad;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Basic(optional = false)
    @NotNull
    @Column(name = "PRECIOUNITARIO")
    private BigDecimal preciounitario;
    @JoinColumn(name = "APARTADOID", referencedColumnName = "ID")
    @ManyToOne(optional = false)
    private Apartado apartadoid;
    @JoinColumn(name = "PLATILLOID", referencedColumnName = "ID")
    @ManyToOne(optional = false)
    private Platillo platilloid;

    public Detalleapartado() {
    }

    public Detalleapartado(Integer id) {
        this.id = id;
    }

    public Detalleapartado(Integer id, int cantidad, BigDecimal preciounitario) {
        this.id = id;
        this.cantidad = cantidad;
        this.preciounitario = preciounitario;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getPreciounitario() {
        return preciounitario;
    }

    public void setPreciounitario(BigDecimal preciounitario) {
        this.preciounitario = preciounitario;
    }

    public Apartado getApartadoid() {
        return apartadoid;
    }

    public void setApartadoid(Apartado apartadoid) {
        this.apartadoid = apartadoid;
    }

    public Platillo getPlatilloid() {
        return platilloid;
    }

    public void setPlatilloid(Platillo platilloid) {
        this.platilloid = platilloid;
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
        if (!(object instanceof Detalleapartado)) {
            return false;
        }
        Detalleapartado other = (Detalleapartado) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "modelo.Detalleapartado[ id=" + id + " ]";
    }
    
}
