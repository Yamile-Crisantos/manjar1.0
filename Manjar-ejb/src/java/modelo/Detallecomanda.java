/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

import java.io.Serializable;
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
@Table(name = "detallecomanda")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Detallecomanda.findAll", query = "SELECT d FROM Detallecomanda d"),
    @NamedQuery(name = "Detallecomanda.findById", query = "SELECT d FROM Detallecomanda d WHERE d.id = :id"),
    @NamedQuery(name = "Detallecomanda.findByCantidad", query = "SELECT d FROM Detallecomanda d WHERE d.cantidad = :cantidad")})
public class Detallecomanda implements Serializable {

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
    @JoinColumn(name = "COMANDAID", referencedColumnName = "ID")
    @ManyToOne(optional = false)
    private Comanda comandaid;
    @JoinColumn(name = "PLATILLOID", referencedColumnName = "ID")
    @ManyToOne(optional = false)
    private Platillo platilloid;

    public Detallecomanda() {
    }

    public Detallecomanda(Integer id) {
        this.id = id;
    }

    public Detallecomanda(Integer id, int cantidad) {
        this.id = id;
        this.cantidad = cantidad;
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

    public Comanda getComandaid() {
        return comandaid;
    }

    public void setComandaid(Comanda comandaid) {
        this.comandaid = comandaid;
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
        if (!(object instanceof Detallecomanda)) {
            return false;
        }
        Detallecomanda other = (Detallecomanda) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "modelo.Detallecomanda[ id=" + id + " ]";
    }
    
}
