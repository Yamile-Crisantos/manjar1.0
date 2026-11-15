/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author yamil
 */
@Entity
@Table(name = "movimientocaja")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Movimientocaja.findAll", query = "SELECT m FROM Movimientocaja m"),
    @NamedQuery(name = "Movimientocaja.findById", query = "SELECT m FROM Movimientocaja m WHERE m.id = :id"),
    @NamedQuery(name = "Movimientocaja.findByTipo", query = "SELECT m FROM Movimientocaja m WHERE m.tipo = :tipo"),
    @NamedQuery(name = "Movimientocaja.findByMonto", query = "SELECT m FROM Movimientocaja m WHERE m.monto = :monto"),
    @NamedQuery(name = "Movimientocaja.findByConcepto", query = "SELECT m FROM Movimientocaja m WHERE m.concepto = :concepto"),
    @NamedQuery(name = "Movimientocaja.findByFechahora", query = "SELECT m FROM Movimientocaja m WHERE m.fechahora = :fechahora")})
public class Movimientocaja implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "ID")
    private Integer id;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 20)
    @Column(name = "TIPO")
    private String tipo;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Basic(optional = false)
    @NotNull
    @Column(name = "MONTO")
    private BigDecimal monto;
    @Size(max = 255)
    @Column(name = "CONCEPTO")
    private String concepto;
    @Column(name = "FECHAHORA")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechahora;
    @JoinColumn(name = "CAJAID", referencedColumnName = "ID")
    @ManyToOne(optional = false)
    private Caja cajaid;
    @JoinColumn(name = "COMANDAID", referencedColumnName = "ID")
    @ManyToOne
    private Comanda comandaid;

    public Movimientocaja() {
    }

    public Movimientocaja(Integer id) {
        this.id = id;
    }

    public Movimientocaja(Integer id, String tipo, BigDecimal monto) {
        this.id = id;
        this.tipo = tipo;
        this.monto = monto;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public String getConcepto() {
        return concepto;
    }

    public void setConcepto(String concepto) {
        this.concepto = concepto;
    }

    public Date getFechahora() {
        return fechahora;
    }

    public void setFechahora(Date fechahora) {
        this.fechahora = fechahora;
    }

    public Caja getCajaid() {
        return cajaid;
    }

    public void setCajaid(Caja cajaid) {
        this.cajaid = cajaid;
    }

    public Comanda getComandaid() {
        return comandaid;
    }

    public void setComandaid(Comanda comandaid) {
        this.comandaid = comandaid;
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
        if (!(object instanceof Movimientocaja)) {
            return false;
        }
        Movimientocaja other = (Movimientocaja) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "modelo.Movimientocaja[ id=" + id + " ]";
    }
    
}
