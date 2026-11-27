package modelo;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * La foto que el dueño sube de un platillo (una por platillo). Se guarda en la misma base mientras no exista el
 * microservicio de almacenamiento de archivos de la plataforma. Siempre es un JPEG generado por el servidor.
 * Va en su propia tabla para que las consultas del menú no arrastren los bytes de la imagen.
 */
@Entity
@Table(name = "imagenplatillo")
@NamedQueries({
    @NamedQuery(name = "Imagenplatillo.findByPlatillo", query = "SELECT i FROM Imagenplatillo i WHERE i.platilloid.id = :p")})
public class Imagenplatillo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "ID")
    private Integer id;

    @JoinColumn(name = "PLATILLOID", referencedColumnName = "ID")
    @OneToOne(optional = false)
    private Platillo platilloid;

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 30)
    @Column(name = "TIPOMIME")
    private String tipomime;

    @Basic(optional = false, fetch = FetchType.LAZY)
    @NotNull
    @Lob
    @Column(name = "DATOS")
    private byte[] datos;

    @Basic(optional = false)
    @Column(name = "TAMANO")
    private int tamano;

    @Basic(optional = false)
    @Column(name = "FECHA")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fecha;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Platillo getPlatilloid() {
        return platilloid;
    }

    public void setPlatilloid(Platillo platilloid) {
        this.platilloid = platilloid;
    }

    public String getTipomime() {
        return tipomime;
    }

    public void setTipomime(String tipomime) {
        this.tipomime = tipomime;
    }

    public byte[] getDatos() {
        return datos;
    }

    public void setDatos(byte[] datos) {
        this.datos = datos;
    }

    public int getTamano() {
        return tamano;
    }

    public void setTamano(int tamano) {
        this.tamano = tamano;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Imagenplatillo)) {
            return false;
        }
        Imagenplatillo other = (Imagenplatillo) object;
        return !((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id)));
    }

    @Override
    public String toString() {
        return "modelo.Imagenplatillo[ id=" + id + " ]";
    }
}
