package logicaneg;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Una comanda lista para mostrarse en pantalla (mesero, cocina, caja). Es una copia simple de los
 * datos: la pantalla no toca las entidades de JPA.
 */
public class ComandaVista implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Una línea de la comanda. */
    public static class Linea implements Serializable {

        private static final long serialVersionUID = 1L;
        private final Integer platilloId;
        private final String nombre;
        private final int cantidad;
        private final BigDecimal precio;
        private final String nota;

        public Linea(Integer platilloId, String nombre, int cantidad, BigDecimal precio, String nota) {
            this.platilloId = platilloId;
            this.nombre = nombre;
            this.cantidad = cantidad;
            this.precio = precio == null ? BigDecimal.ZERO : precio;
            this.nota = nota;
        }

        public Integer getPlatilloId() {
            return platilloId;
        }

        public String getNombre() {
            return nombre;
        }

        public int getCantidad() {
            return cantidad;
        }

        public BigDecimal getPrecio() {
            return precio;
        }

        public String getNota() {
            return nota;
        }

        public BigDecimal getSubtotal() {
            return precio.multiply(BigDecimal.valueOf(cantidad));
        }
    }

    private Integer id;
    private String tipo;
    private String mesa;
    private String estado;
    private Date fechaHora;
    private Date fechaCobro;
    private int minutosEspera;
    private Integer meseroId;
    private String meseroNombre;
    private final List<Linea> lineas = new ArrayList<>();

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

    public Date getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(Date fechaHora) {
        this.fechaHora = fechaHora;
    }

    public Date getFechaCobro() {
        return fechaCobro;
    }

    public void setFechaCobro(Date fechaCobro) {
        this.fechaCobro = fechaCobro;
    }

    public int getMinutosEspera() {
        return minutosEspera;
    }

    public void setMinutosEspera(int minutosEspera) {
        this.minutosEspera = minutosEspera;
    }

    public Integer getMeseroId() {
        return meseroId;
    }

    public void setMeseroId(Integer meseroId) {
        this.meseroId = meseroId;
    }

    public String getMeseroNombre() {
        return meseroNombre;
    }

    public void setMeseroNombre(String meseroNombre) {
        this.meseroNombre = meseroNombre;
    }

    public List<Linea> getLineas() {
        return lineas;
    }

    // ------------------------------------------------------------------
    // Derivados para la pantalla
    // ------------------------------------------------------------------
    /** "Mesa 4", "Comer aquí · Juan" o "Para llevar · Ana". */
    public String getEtiqueta() {
        return etiqueta(tipo, mesa);
    }

    /**
     * Cómo se nombra una comanda: "Para llevar · Ana", "Mesa 4", "Mesa Terraza 2" o, si lo escrito no tiene
     * ningún número (un nombre), "Comer aquí · Juan". El campo se llama "Mesa o nombre".
     */
    public static String etiqueta(String tipo, String mesa) {
        boolean vacia = mesa == null || mesa.trim().isEmpty();
        if (Constantes.TIPO_LLEVAR.equals(tipo)) {
            return vacia ? "Para llevar" : "Para llevar · " + mesa;
        }
        if (vacia) {
            return "Comer aquí";
        }
        String m = mesa.trim();
        if (m.toLowerCase().startsWith("mesa")) {
            return m;
        }
        return m.matches(".*\\d.*") ? "Mesa " + m : "Comer aquí · " + m;
    }

    public boolean isLlevar() {
        return Constantes.TIPO_LLEVAR.equals(tipo);
    }

    public String getEstadoTexto() {
        if (estado == null) {
            return "";
        }
        switch (estado) {
            case Constantes.COMANDA_PENDIENTE:
                return "Pendiente";
            case Constantes.COMANDA_EN_PREPARACION:
                return "En preparación";
            case Constantes.COMANDA_LISTA:
                return "Lista";
            case Constantes.COMANDA_ENTREGADA:
                return "Entregada";
            case Constantes.COMANDA_CANCELADA:
                return "Cancelada";
            default:
                return estado;
        }
    }

    public boolean isPendiente() {
        return Constantes.COMANDA_PENDIENTE.equals(estado);
    }

    public boolean isEnPreparacion() {
        return Constantes.COMANDA_EN_PREPARACION.equals(estado);
    }

    public boolean isLista() {
        return Constantes.COMANDA_LISTA.equals(estado);
    }

    public boolean isEntregada() {
        return Constantes.COMANDA_ENTREGADA.equals(estado);
    }

    public boolean isCancelada() {
        return Constantes.COMANDA_CANCELADA.equals(estado);
    }

    public boolean isCobrada() {
        return fechaCobro != null;
    }

    public BigDecimal getTotal() {
        BigDecimal t = BigDecimal.ZERO;
        for (Linea l : lineas) {
            t = t.add(l.getSubtotal());
        }
        return t;
    }

    public int getPorciones() {
        int n = 0;
        for (Linea l : lineas) {
            n += l.getCantidad();
        }
        return n;
    }
}
