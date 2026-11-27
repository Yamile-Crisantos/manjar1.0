package logicaneg;

import java.io.Serializable;

/** Un negocio donde el cliente ya apartó, para volver a él con un toque. */
public class NegocioVista implements Serializable {

    private static final long serialVersionUID = 1L;
    private final Integer id;
    private final String nombre;
    private final String codigo;
    private final int apartados;

    public NegocioVista(Integer id, String nombre, String codigo, int apartados) {
        this.id = id;
        this.nombre = nombre;
        this.codigo = codigo;
        this.apartados = apartados;
    }

    public Integer getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    /** El código del QR del negocio (el que va en ?q=). */
    public String getCodigo() {
        return codigo;
    }

    /** Cuántos apartados ha hecho ahí el cliente. */
    public int getApartados() {
        return apartados;
    }
}
