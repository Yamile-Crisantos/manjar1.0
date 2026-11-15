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
@Table(name = "comanda")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Comanda.findAll", query = "SELECT c FROM Comanda c"),
    @NamedQuery(name = "Comanda.findById", query = "SELECT c FROM Comanda c WHERE c.id = :id"),
    @NamedQuery(name = "Comanda.findByMesa", query = "SELECT c FROM Comanda c WHERE c.mesa = :mesa"),
    @NamedQuery(name = "Comanda.findByEstado", query = "SELECT c FROM Comanda c WHERE c.estado = :estado")})
public class Comanda implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "ID")
    private Integer id;
    @Size(max = 50)
    @Column(name = "MESA")
    private String mesa;
    @Size(max = 20)
    @Column(name = "ESTADO")
    private String estado;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "comandaid")
    private List<Detallecomanda> detallecomandaList;
    @JoinColumn(name = "MESEROID", referencedColumnName = "ID")
    @ManyToOne(optional = false)
    private Usuario meseroid;
    @OneToMany(mappedBy = "comandaid")
    private List<Movimientocaja> movimientocajaList;

    public Comanda() {
    }

    public Comanda(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMesa() {
        return mesa;
    }

    public void setMesa(String mesa) {
        this.mesa = mesa;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    @XmlTransient
    public List<Detallecomanda> getDetallecomandaList() {
        return detallecomandaList;
    }

    public void setDetallecomandaList(List<Detallecomanda> detallecomandaList) {
        this.detallecomandaList = detallecomandaList;
    }

    public Usuario getMeseroid() {
        return meseroid;
    }

    public void setMeseroid(Usuario meseroid) {
        this.meseroid = meseroid;
    }

    @XmlTransient
    public List<Movimientocaja> getMovimientocajaList() {
        return movimientocajaList;
    }

    public void setMovimientocajaList(List<Movimientocaja> movimientocajaList) {
        this.movimientocajaList = movimientocajaList;
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
        if (!(object instanceof Comanda)) {
            return false;
        }
        Comanda other = (Comanda) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "modelo.Comanda[ id=" + id + " ]";
    }
    
}
