package logicaneg;

import java.io.Serializable;
import java.util.Date;

/** La foto subida de un platillo, lista para servirse. Siempre es un JPEG generado por el servidor. */
public class FotoPlatillo implements Serializable {

    private static final long serialVersionUID = 1L;
    private final String tipo;
    private final byte[] datos;
    private final Date fecha;

    public FotoPlatillo(String tipo, byte[] datos, Date fecha) {
        this.tipo = tipo;
        this.datos = datos;
        this.fecha = fecha;
    }

    public String getTipo() {
        return tipo;
    }

    public byte[] getDatos() {
        return datos;
    }

    public Date getFecha() {
        return fecha;
    }
}
