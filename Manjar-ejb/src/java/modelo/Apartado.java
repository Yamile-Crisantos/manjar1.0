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
@Table(name = "apartado")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Apartado.findAll", query = "SELECT a FROM Apartado a"),
    @NamedQuery(name = "Apartado.findById", query = "SELECT a FROM Apartado a WHERE a.id = :id"),
    @NamedQuery(name = "Apartado.findByFechacreacion", query = "SELECT a FROM Apartado a WHERE a.fechacreacion = :fechacreacion"),
    @NamedQuery(name = "Apartado.findByHorarecoleccion", query = "SELECT a FROM Apartado a WHERE a.horarecoleccion = :horarecoleccion"),
    @NamedQuery(name = "Apartado.findByEstado", query = "SELECT a FROM Apartado a WHERE a.estado = :estado"),
    @NamedQuery(name = "Apartado.findByTotal", query = "SELECT a FROM Apartado a WHERE a.total = :total"),
    @NamedQuery(name = "Apartado.findByCodigocomprobante", query = "SELECT a FROM Apartado a WHERE a.codigocomprobante = :codigocomprobante")})
public class Apartado implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "ID")
    private Integer id;
    @Column(name = "FECHACREACION")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechacreacion;
    @Basic(optional = false)
    @NotNull
    @Column(name = "HORARECOLECCION")
    @Temporal(TemporalType.TIMESTAMP)
    private Date horarecoleccion;
    @Size(max = 20)
    @Column(name = "ESTADO")
    private String estado;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Basic(optional = false)
    @NotNull
    @Column(name = "TOTAL")
    private BigDecimal total;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 20)
    @Column(name = "CODIGOCOMPROBANTE")
    private String codigocomprobante;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "apartadoid")
    private List<Detalleapartado> detalleapartadoList;
    @JoinColumn(name = "ESTABLECIMIENTOID", referencedColumnName = "ID")
    @ManyToOne(optional = false)
    private Establecimiento establecimientoid;
    @JoinColumn(name = "CLIENTEID", referencedColumnName = "ID")
    @ManyToOne(optional = false)
    private Usuario clienteid;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "apartadoid")
    private List<Pago> pagoList;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "apartadoid")
    private List<Notificacion> notificacionList;

    /** Cuándo se entregó al cliente. */
    @Column(name = "FECHAENTREGA")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaentrega;

    /** Quién confirmó la entrega. */
    @JoinColumn(name = "ENTREGADOPORID", referencedColumnName = "ID")
    @ManyToOne
    private Usuario entregadoporid;

    public Apartado() {
    }

    public Apartado(Integer id) {
        this.id = id;
    }

    public Apartado(Integer id, Date horarecoleccion, BigDecimal total, String codigocomprobante) {
        this.id = id;
        this.horarecoleccion = horarecoleccion;
        this.total = total;
        this.codigocomprobante = codigocomprobante;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getFechacreacion() {
        return fechacreacion;
    }

    public void setFechacreacion(Date fechacreacion) {
        this.fechacreacion = fechacreacion;
    }

    public Date getHorarecoleccion() {
        return horarecoleccion;
    }

    public void setHorarecoleccion(Date horarecoleccion) {
        this.horarecoleccion = horarecoleccion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getCodigocomprobante() {
        return codigocomprobante;
    }

    public void setCodigocomprobante(String codigocomprobante) {
        this.codigocomprobante = codigocomprobante;
    }

    @XmlTransient
    public List<Detalleapartado> getDetalleapartadoList() {
        return detalleapartadoList;
    }

    public void setDetalleapartadoList(List<Detalleapartado> detalleapartadoList) {
        this.detalleapartadoList = detalleapartadoList;
    }

    public Establecimiento getEstablecimientoid() {
        return establecimientoid;
    }

    public void setEstablecimientoid(Establecimiento establecimientoid) {
        this.establecimientoid = establecimientoid;
    }

    public Usuario getClienteid() {
        return clienteid;
    }

    public void setClienteid(Usuario clienteid) {
        this.clienteid = clienteid;
    }

    @XmlTransient
    public List<Pago> getPagoList() {
        return pagoList;
    }

    public void setPagoList(List<Pago> pagoList) {
        this.pagoList = pagoList;
    }

    @XmlTransient
    public List<Notificacion> getNotificacionList() {
        return notificacionList;
    }

    public void setNotificacionList(List<Notificacion> notificacionList) {
        this.notificacionList = notificacionList;
    }

    public Date getFechaentrega() {
        return fechaentrega;
    }

    public void setFechaentrega(Date fechaentrega) {
        this.fechaentrega = fechaentrega;
    }

    public Usuario getEntregadoporid() {
        return entregadoporid;
    }

    public void setEntregadoporid(Usuario entregadoporid) {
        this.entregadoporid = entregadoporid;
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
        if (!(object instanceof Apartado)) {
            return false;
        }
        Apartado other = (Apartado) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "modelo.Apartado[ id=" + id + " ]";
    }
    
}
