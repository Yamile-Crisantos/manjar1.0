/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author yamil
 */
@Entity
@Table(name = "caja")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Caja.findAll", query = "SELECT c FROM Caja c"),
    @NamedQuery(name = "Caja.findById", query = "SELECT c FROM Caja c WHERE c.id = :id"),
    @NamedQuery(name = "Caja.findByFechaapertura", query = "SELECT c FROM Caja c WHERE c.fechaapertura = :fechaapertura"),
    @NamedQuery(name = "Caja.findByMontoinicial", query = "SELECT c FROM Caja c WHERE c.montoinicial = :montoinicial"),
    @NamedQuery(name = "Caja.findByEstado", query = "SELECT c FROM Caja c WHERE c.estado = :estado"),
    @NamedQuery(name = "Caja.findByFechacierre", query = "SELECT c FROM Caja c WHERE c.fechacierre = :fechacierre"),
    @NamedQuery(name = "Caja.findByMontoesperado", query = "SELECT c FROM Caja c WHERE c.montoesperado = :montoesperado"),
    @NamedQuery(name = "Caja.findByMontocontado", query = "SELECT c FROM Caja c WHERE c.montocontado = :montocontado"),
    @NamedQuery(name = "Caja.findByDiferencia", query = "SELECT c FROM Caja c WHERE c.diferencia = :diferencia")})
public class Caja implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "ID")
    private Integer id;
    @Column(name = "FECHAAPERTURA")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaapertura;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Basic(optional = false)
    @NotNull
    @Column(name = "MONTOINICIAL")
    private BigDecimal montoinicial;
    @Size(max = 20)
    @Column(name = "ESTADO")
    private String estado;
    @Column(name = "FECHACIERRE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechacierre;
    @Column(name = "MONTOESPERADO")
    private BigDecimal montoesperado;
    @Column(name = "MONTOCONTADO")
    private BigDecimal montocontado;
    @Column(name = "DIFERENCIA")
    private BigDecimal diferencia;
    @JoinColumn(name = "ESTABLECIMIENTOID", referencedColumnName = "ID")
    @ManyToOne(optional = false)
    private Establecimiento establecimientoid;
    @JoinColumn(name = "USUARIOID", referencedColumnName = "ID")
    @ManyToOne(optional = false)
    private Usuario usuarioid;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "cajaid")
    private List<Movimientocaja> movimientocajaList;

    /** Quién hizo el corte y cerró la caja. */
    @JoinColumn(name = "CERRADAPORID", referencedColumnName = "ID")
    @ManyToOne
    private Usuario cerradaporid;

    public Caja() {
    }

    public Caja(Integer id) {
        this.id = id;
    }

    public Caja(Integer id, BigDecimal montoinicial) {
        this.id = id;
        this.montoinicial = montoinicial;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getFechaapertura() {
        return fechaapertura;
    }

    public void setFechaapertura(Date fechaapertura) {
        this.fechaapertura = fechaapertura;
    }

    public BigDecimal getMontoinicial() {
        return montoinicial;
    }

    public void setMontoinicial(BigDecimal montoinicial) {
        this.montoinicial = montoinicial;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Date getFechacierre() {
        return fechacierre;
    }

    public void setFechacierre(Date fechacierre) {
        this.fechacierre = fechacierre;
    }

    public BigDecimal getMontoesperado() {
        return montoesperado;
    }

    public void setMontoesperado(BigDecimal montoesperado) {
        this.montoesperado = montoesperado;
    }

    public BigDecimal getMontocontado() {
        return montocontado;
    }

    public void setMontocontado(BigDecimal montocontado) {
        this.montocontado = montocontado;
    }

    public BigDecimal getDiferencia() {
        return diferencia;
    }

    public void setDiferencia(BigDecimal diferencia) {
        this.diferencia = diferencia;
    }

    public Establecimiento getEstablecimientoid() {
        return establecimientoid;
    }

    public void setEstablecimientoid(Establecimiento establecimientoid) {
        this.establecimientoid = establecimientoid;
    }

    public Usuario getUsuarioid() {
        return usuarioid;
    }

    public void setUsuarioid(Usuario usuarioid) {
        this.usuarioid = usuarioid;
    }

    @XmlTransient
    public List<Movimientocaja> getMovimientocajaList() {
        return movimientocajaList;
    }

    public void setMovimientocajaList(List<Movimientocaja> movimientocajaList) {
        this.movimientocajaList = movimientocajaList;
    }

    public Usuario getCerradaporid() {
        return cerradaporid;
    }

    public void setCerradaporid(Usuario cerradaporid) {
        this.cerradaporid = cerradaporid;
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
        if (!(object instanceof Caja)) {
            return false;
        }
        Caja other = (Caja) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "modelo.Caja[ id=" + id + " ]";
    }
    
}
