package logicaneg;

import java.io.Serializable;
import java.math.BigDecimal;

/** Un platillo del menú tal como se muestra al pedirlo (comandas y apartados). */
public class ItemMenu implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Integer id;
    private final String nombre;
    private final String descripcion;
    private final String categoria;
    private final BigDecimal precio;
    /** Porciones que quedan; null si el platillo no lleva control de porciones. */
    private final Integer restante;

    public ItemMenu(Integer id, String nombre, String descripcion, String categoria, BigDecimal precio, Integer restante) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.precio = precio;
        this.restante = restante;
    }

    public Integer getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getCategoria() {
        return categoria;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public Integer getRestante() {
        return restante;
    }

    public boolean isConControl() {
        return restante != null;
    }
}
