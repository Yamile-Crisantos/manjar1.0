package logicaneg;

import java.io.Serializable;

/**
 * Una línea de lo que se pide: cuántas porciones de qué platillo, con una indicación opcional
 * para cocina ("sin cebolla"). La usan las comandas (HU-08) y los apartados (HU-13).
 */
public class LineaPedido implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer platilloId;
    private int cantidad;
    private String nota;

    public LineaPedido() {
    }

    public LineaPedido(Integer platilloId, int cantidad, String nota) {
        this.platilloId = platilloId;
        this.cantidad = cantidad;
        this.nota = nota;
    }

    public Integer getPlatilloId() {
        return platilloId;
    }

    public void setPlatilloId(Integer platilloId) {
        this.platilloId = platilloId;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public String getNota() {
        return nota;
    }

    public void setNota(String nota) {
        this.nota = nota;
    }
}
