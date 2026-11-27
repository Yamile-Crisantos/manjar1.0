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
    /** Foto: un enlace http/https o la ruta interna de una foto subida; null si no tiene. */
    private final String imagenUrl;

    public ItemMenu(Integer id, String nombre, String descripcion, String categoria, BigDecimal precio, Integer restante) {
        this(id, nombre, descripcion, categoria, precio, restante, null);
    }

    public ItemMenu(Integer id, String nombre, String descripcion, String categoria, BigDecimal precio, Integer restante,
            String imagenUrl) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.precio = precio;
        this.restante = restante;
        this.imagenUrl = imagenUrl == null || imagenUrl.trim().isEmpty() ? null : imagenUrl;
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

    public String getImagenUrl() {
        return imagenUrl;
    }

    public boolean isConImagen() {
        return imagenUrl != null;
    }

    public boolean isConControl() {
        return restante != null;
    }
}
