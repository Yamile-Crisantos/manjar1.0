package controlador;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import logicaneg.ComandaVista;
import logicaneg.Constantes;
import logicaneg.ItemMenu;
import logicaneg.LineaPedido;
import logicaneg.MComanda;
import logicaneg.MPlatillo;
import logicaneg.ReglaNegocioException;

/**
 * Comandas del mesero: HU-08 (registrar el pedido de una mesa o para llevar) y
 * HU-09 (ver el estado de cada comanda).
 *
 * La sucursal y el usuario SIEMPRE salen de la sesión. Las listas se guardan en la vista (no se
 * vuelven a consultar en cada pregunta de JSF) para que el botón que se presiona corresponda
 * siempre a la fila que el usuario tiene en pantalla; se renuevan después de cada acción y con
 * la actualización automática (p:poll).
 */
@Named("comandasBean")
@ViewScoped
public class ComandasBean implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Platillos de una misma categoría, para mostrarlos juntos. */
    public static class GrupoMenu implements Serializable {

        private static final long serialVersionUID = 1L;
        private final String categoria;
        private final List<ItemMenu> items = new ArrayList<>();

        GrupoMenu(String categoria) {
            this.categoria = categoria;
        }

        public String getCategoria() {
            return categoria;
        }

        public List<ItemMenu> getItems() {
            return items;
        }
    }

    /** Una línea del pedido que se está armando (todavía no se envía a cocina). */
    public static class LineaCarrito implements Serializable {

        private static final long serialVersionUID = 1L;
        private final Integer platilloId;
        private final String nombre;
        private final BigDecimal precio;
        private int cantidad;
        private String nota;

        LineaCarrito(Integer platilloId, String nombre, BigDecimal precio) {
            this.platilloId = platilloId;
            this.nombre = nombre;
            this.precio = precio;
            this.cantidad = 1;
        }

        public Integer getPlatilloId() {
            return platilloId;
        }

        public String getNombre() {
            return nombre;
        }

        public BigDecimal getPrecio() {
            return precio;
        }

        public int getCantidad() {
            return cantidad;
        }

        public String getNota() {
            return nota;
        }

        public void setNota(String nota) {
            this.nota = nota;
        }

        public BigDecimal getSubtotal() {
            return precio.multiply(BigDecimal.valueOf(cantidad));
        }
    }

    @EJB
    private MComanda mComanda;

    @EJB
    private MPlatillo mPlatillo;

    @Inject
    private SesionBean sesion;

    private String tipo = Constantes.TIPO_MESA;
    private String mesa;
    private final List<LineaCarrito> carrito = new ArrayList<>();

    private List<GrupoMenu> grupos = new ArrayList<>();
    private final Map<Integer, ItemMenu> menuPorId = new LinkedHashMap<>();
    private List<ComandaVista> activas = new ArrayList<>();
    private List<ComandaVista> terminadas = new ArrayList<>();
    private boolean soloMias;

    @PostConstruct
    public void init() {
        soloMias = sesion.isMesero();
        refrescar();
    }

    /** Vuelve a leer el menú disponible y las comandas (lo llama también la actualización automática). */
    public void refrescar() {
        Integer est = sesion.getEstablecimientoId();
        if (est == null) {
            return;
        }
        menuPorId.clear();
        grupos = new ArrayList<>();
        Map<String, GrupoMenu> porCategoria = new LinkedHashMap<>();
        for (ItemMenu it : mPlatillo.disponibles(est)) {
            menuPorId.put(it.getId(), it);
            GrupoMenu g = porCategoria.get(it.getCategoria());
            if (g == null) {
                g = new GrupoMenu(it.getCategoria());
                porCategoria.put(it.getCategoria(), g);
                grupos.add(g);
            }
            g.getItems().add(it);
        }
        Integer filtro = filtroMesero();
        activas = mComanda.activas(est, filtro);
        terminadas = mComanda.terminadasHoy(est, filtro);
    }

    private Integer filtroMesero() {
        return (sesion.isMesero() || soloMias) ? sesion.getUsuarioId() : null;
    }

    // ------------------------------------------------------------------
    // Armar el pedido
    // ------------------------------------------------------------------
    public void agregar(Integer platilloId) {
        ItemMenu it = menuPorId.get(platilloId);
        if (it == null) {
            Mensajes.aviso("Ese platillo ya no está disponible.");
            refrescar();
            return;
        }
        LineaCarrito l = buscarLinea(platilloId);
        if (l == null) {
            carrito.add(new LineaCarrito(it.getId(), it.getNombre(), it.getPrecio()));
        } else {
            subir(l, it);
        }
    }

    public void mas(LineaCarrito l) {
        subir(l, menuPorId.get(l.getPlatilloId()));
    }

    public void menos(LineaCarrito l) {
        if (l.cantidad > 1) {
            l.cantidad--;
        } else {
            carrito.remove(l);
        }
    }

    public void quitar(LineaCarrito l) {
        carrito.remove(l);
    }

    private void subir(LineaCarrito l, ItemMenu it) {
        if (l.cantidad >= MComanda.MAX_CANTIDAD) {
            Mensajes.aviso("Máximo " + MComanda.MAX_CANTIDAD + " porciones por platillo.");
            return;
        }
        if (it != null && it.getRestante() != null && l.cantidad + 1 > it.getRestante()) {
            Mensajes.aviso("De \"" + it.getNombre() + "\" solo quedan " + it.getRestante()
                    + (it.getRestante() == 1 ? " porción." : " porciones."));
            return;
        }
        l.cantidad++;
    }

    private LineaCarrito buscarLinea(Integer platilloId) {
        for (LineaCarrito l : carrito) {
            if (l.platilloId.equals(platilloId)) {
                return l;
            }
        }
        return null;
    }

    /** Envía el pedido a cocina (HU-08). */
    public void enviar() {
        List<LineaPedido> pedido = new ArrayList<>();
        for (LineaCarrito l : carrito) {
            pedido.add(new LineaPedido(l.platilloId, l.cantidad, l.nota));
        }
        try {
            ComandaVista c = mComanda.crear(sesion.getEstablecimientoId(), sesion.getUsuarioId(), tipo, mesa, pedido);
            Mensajes.info("Comanda #" + c.getId() + " enviada a cocina (" + c.getEtiqueta() + ").");
            carrito.clear();
            mesa = null;
        } catch (ReglaNegocioException e) {
            Mensajes.error(e.getMessage());
        }
        refrescar();
    }

    // ------------------------------------------------------------------
    // Comandas ya enviadas
    // ------------------------------------------------------------------
    public void entregar(Integer comandaId) {
        try {
            ComandaVista c = mComanda.entregar(sesion.getEstablecimientoId(), sesion.getUsuarioId(), comandaId);
            Mensajes.info("Comanda #" + c.getId() + " marcada como entregada.");
        } catch (ReglaNegocioException e) {
            Mensajes.error(e.getMessage());
        }
        refrescar();
    }

    public void cancelar(Integer comandaId) {
        try {
            ComandaVista c = mComanda.cancelar(sesion.getEstablecimientoId(), sesion.getUsuarioId(), comandaId);
            Mensajes.info("Comanda #" + c.getId() + " cancelada. Las porciones regresaron al menú.");
        } catch (ReglaNegocioException e) {
            Mensajes.error(e.getMessage());
        }
        refrescar();
    }

    /** true si quien tiene la sesión puede cancelar esa comanda ahora (el servidor lo vuelve a comprobar). */
    public boolean puedeCancelar(ComandaVista c) {
        if (c.isCancelada() || c.isCobrada()) {
            return false;
        }
        if (sesion.isAdministra()) {
            return true;
        }
        return c.isPendiente() && sesion.getUsuarioId() != null && sesion.getUsuarioId().equals(c.getMeseroId());
    }

    // ------------------------------------------------------------------
    // Datos para la pantalla
    // ------------------------------------------------------------------
    public BigDecimal getTotalCarrito() {
        BigDecimal t = BigDecimal.ZERO;
        for (LineaCarrito l : carrito) {
            t = t.add(l.getSubtotal());
        }
        return t;
    }

    public int getPorcionesCarrito() {
        int n = 0;
        for (LineaCarrito l : carrito) {
            n += l.cantidad;
        }
        return n;
    }

    public boolean isLlevar() {
        return Constantes.TIPO_LLEVAR.equals(tipo);
    }

    public List<LineaCarrito> getCarrito() {
        return carrito;
    }

    public List<GrupoMenu> getGrupos() {
        return grupos;
    }

    public List<ComandaVista> getActivas() {
        return activas;
    }

    public List<ComandaVista> getTerminadas() {
        return terminadas;
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

    public boolean isSoloMias() {
        return soloMias;
    }

    public void setSoloMias(boolean soloMias) {
        this.soloMias = soloMias;
    }

    /** El gerente y el dueño pueden alternar entre "todas" y "solo las mías". */
    public boolean isPuedeFiltrar() {
        return sesion.isAdministra();
    }
}
