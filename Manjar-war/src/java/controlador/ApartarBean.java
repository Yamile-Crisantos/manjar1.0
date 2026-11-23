package controlador;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import logicaneg.ApartadoVista;
import logicaneg.ItemMenu;
import logicaneg.LineaPedido;
import logicaneg.MApartado;
import logicaneg.MPlatillo;
import logicaneg.NotificacionVista;
import logicaneg.ReglaNegocioException;
import modelo.Establecimiento;

/**
 * Pantalla del cliente: HU-13 (apartar platillos de un negocio y recibir el comprobante con su código),
 * HU-17 (avisos, por ejemplo "tu pedido ya está listo") y "Mis apartados".
 *
 * El negocio sale del último menú que el cliente abrió con el QR (o del que trae el enlace ?q=). Se guarda
 * en la vista al abrirla, porque en las acciones de la pantalla (AJAX) el parámetro de la URL ya no viene.
 * El cliente de la sesión nunca se toma de la pantalla: MApartado lo vuelve a comprobar.
 */
@Named("apartarBean")
@ViewScoped
public class ApartarBean implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Platillos de una categoría, para mostrarlos juntos. */
    public static class Grupo implements Serializable {

        private static final long serialVersionUID = 1L;
        private final String categoria;
        private final List<ItemMenu> items = new ArrayList<>();

        Grupo(String categoria) {
            this.categoria = categoria;
        }

        public String getCategoria() {
            return categoria;
        }

        public List<ItemMenu> getItems() {
            return items;
        }
    }

    /** Un platillo elegido por el cliente. */
    public static class Linea implements Serializable {

        private static final long serialVersionUID = 1L;
        private final Integer platilloId;
        private final String nombre;
        private final BigDecimal precio;
        private int cantidad = 1;

        Linea(Integer platilloId, String nombre, BigDecimal precio) {
            this.platilloId = platilloId;
            this.nombre = nombre;
            this.precio = precio;
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

        public BigDecimal getSubtotal() {
            return precio.multiply(BigDecimal.valueOf(cantidad));
        }
    }

    @EJB
    private MApartado mApartado;

    @EJB
    private MPlatillo mPlatillo;

    @Inject
    private SesionBean sesion;

    @Inject
    private MenuPublicoBean menuPublicoBean;

    private Integer negocioId;
    private String negocioNombre;
    private String negocioCodigo;

    private final List<Linea> carrito = new ArrayList<>();
    private List<Grupo> grupos = new ArrayList<>();
    private final Map<Integer, ItemMenu> menuPorId = new LinkedHashMap<>();
    private List<String> horas = new ArrayList<>();
    private String hora;

    private List<ApartadoVista> misApartados = new ArrayList<>();
    private List<NotificacionVista> avisos = new ArrayList<>();
    private int sinLeer;
    /** El apartado que se acaba de crear, para destacarlo con su comprobante. */
    private Integer recienCreado;

    @PostConstruct
    public void init() {
        Establecimiento e = menuPublicoBean.getMarca();
        if (e != null) {
            negocioId = e.getId();
            negocioNombre = e.getNombre();
            negocioCodigo = e.getCodigoQR();
        }
        refrescarMenu();
        refrescarMis();
    }

    // ------------------------------------------------------------------
    // Menú y pedido
    // ------------------------------------------------------------------
    public void refrescarMenu() {
        menuPorId.clear();
        grupos = new ArrayList<>();
        if (negocioId != null) {
            Map<String, Grupo> porCategoria = new LinkedHashMap<>();
            for (ItemMenu it : mPlatillo.disponibles(negocioId)) {
                menuPorId.put(it.getId(), it);
                Grupo g = porCategoria.get(it.getCategoria());
                if (g == null) {
                    g = new Grupo(it.getCategoria());
                    porCategoria.put(it.getCategoria(), g);
                    grupos.add(g);
                }
                g.getItems().add(it);
            }
        }
        calcularHoras();
    }

    /** Horas de recolección de hoy: cada 15 minutos, empezando con la anticipación mínima. */
    private void calcularHoras() {
        List<String> nuevas = new ArrayList<>();
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MINUTE, MApartado.ANTICIPACION_MIN);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        int resto = c.get(Calendar.MINUTE) % 15;
        if (resto != 0 || c.get(Calendar.SECOND) != 0) {
            c.add(Calendar.MINUTE, 15 - resto);
        }
        int dia = c.get(Calendar.DAY_OF_YEAR);
        while (c.get(Calendar.DAY_OF_YEAR) == dia && nuevas.size() < 48) {
            nuevas.add(String.format("%02d:%02d", c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE)));
            c.add(Calendar.MINUTE, 15);
        }
        horas = nuevas;
        if (hora == null || !horas.contains(hora)) {
            hora = horas.isEmpty() ? null : horas.get(0);
        }
    }

    public void agregar(Integer platilloId) {
        ItemMenu it = menuPorId.get(platilloId);
        if (it == null) {
            Mensajes.aviso("Ese platillo ya no está disponible.");
            refrescarMenu();
            return;
        }
        Linea l = buscarLinea(platilloId);
        if (l == null) {
            carrito.add(new Linea(it.getId(), it.getNombre(), it.getPrecio()));
        } else {
            mas(l);
        }
    }

    public void mas(Linea l) {
        ItemMenu it = menuPorId.get(l.platilloId);
        if (l.cantidad >= MApartado.MAX_CANTIDAD) {
            Mensajes.aviso("Máximo " + MApartado.MAX_CANTIDAD + " porciones por platillo.");
            return;
        }
        if (it != null && it.getRestante() != null && l.cantidad + 1 > it.getRestante()) {
            Mensajes.aviso("De \"" + it.getNombre() + "\" solo quedan " + it.getRestante()
                    + (it.getRestante() == 1 ? " porción." : " porciones."));
            return;
        }
        l.cantidad++;
    }

    public void menos(Linea l) {
        if (l.cantidad > 1) {
            l.cantidad--;
        } else {
            carrito.remove(l);
        }
    }

    public void quitar(Linea l) {
        carrito.remove(l);
    }

    private Linea buscarLinea(Integer platilloId) {
        for (Linea l : carrito) {
            if (l.platilloId.equals(platilloId)) {
                return l;
            }
        }
        return null;
    }

    /** Crea el apartado (HU-13). */
    public void apartar() {
        if (negocioId == null) {
            Mensajes.error("Primero abre el menú de un negocio con su código QR.");
            return;
        }
        List<LineaPedido> pedido = new ArrayList<>();
        for (Linea l : carrito) {
            pedido.add(new LineaPedido(l.platilloId, l.cantidad, null));
        }
        try {
            ApartadoVista a = mApartado.crear(sesion.getUsuarioId(), negocioId, pedido, fechaDeHora(hora));
            recienCreado = a.getId();
            carrito.clear();
            Mensajes.info("Apartado creado. Tu código es " + a.getCodigoVisible() + ".");
        } catch (ReglaNegocioException e) {
            Mensajes.error(e.getMessage());
        }
        refrescarMenu();
        refrescarMis();
    }

    /** "HH:mm" de hoy como fecha; null si no es válida. */
    private Date fechaDeHora(String hhmm) {
        if (hhmm == null || !hhmm.matches("\\d{2}:\\d{2}")) {
            return null;
        }
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hhmm.substring(0, 2)));
        c.set(Calendar.MINUTE, Integer.parseInt(hhmm.substring(3, 5)));
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }

    // ------------------------------------------------------------------
    // Mis apartados y avisos
    // ------------------------------------------------------------------
    public void refrescarMis() {
        try {
            misApartados = mApartado.delCliente(sesion.getUsuarioId());
            List<NotificacionVista> todos = mApartado.notificaciones(sesion.getUsuarioId());
            avisos = todos.size() > 5 ? new ArrayList<>(todos.subList(0, 5)) : todos; // los 5 más recientes
            sinLeer = mApartado.noLeidas(sesion.getUsuarioId());
        } catch (ReglaNegocioException e) {
            Mensajes.error(e.getMessage());
        }
    }

    /** Lo usa la actualización automática: renueva lo que cambia sin que el cliente haga nada. */
    public void sondear() {
        refrescarMis();
        refrescarMenu();
    }

    public void cancelar(Integer apartadoId) {
        try {
            ApartadoVista a = mApartado.cancelarComoCliente(sesion.getUsuarioId(), apartadoId);
            Mensajes.info("Apartado " + a.getCodigoVisible() + " cancelado. Las porciones regresaron al menú.");
        } catch (ReglaNegocioException e) {
            Mensajes.error(e.getMessage());
        }
        refrescarMenu();
        refrescarMis();
    }

    public void marcarLeida(Integer notificacionId) {
        try {
            mApartado.marcarLeida(sesion.getUsuarioId(), notificacionId);
        } catch (ReglaNegocioException e) {
            Mensajes.error(e.getMessage());
        }
        refrescarMis();
    }

    public void marcarTodasLeidas() {
        try {
            mApartado.marcarTodasLeidas(sesion.getUsuarioId());
        } catch (ReglaNegocioException e) {
            Mensajes.error(e.getMessage());
        }
        refrescarMis();
    }

    // ------------------------------------------------------------------
    // Para la vista
    // ------------------------------------------------------------------
    public boolean isHayNegocio() {
        return negocioId != null;
    }

    public String getNegocioNombre() {
        return negocioNombre;
    }

    public String getNegocioCodigo() {
        return negocioCodigo;
    }

    public BigDecimal getTotalCarrito() {
        BigDecimal t = BigDecimal.ZERO;
        for (Linea l : carrito) {
            t = t.add(l.getSubtotal());
        }
        return t;
    }

    public int getPorcionesCarrito() {
        int n = 0;
        for (Linea l : carrito) {
            n += l.cantidad;
        }
        return n;
    }

    public int getMinutosRetencion() {
        return MApartado.RETENCION_MIN;
    }

    public List<Linea> getCarrito() {
        return carrito;
    }

    public List<Grupo> getGrupos() {
        return grupos;
    }

    public List<String> getHoras() {
        return horas;
    }

    public String getHora() {
        return hora;
    }

    public void setHora(String hora) {
        this.hora = hora;
    }

    public List<ApartadoVista> getMisApartados() {
        return misApartados;
    }

    public List<NotificacionVista> getAvisos() {
        return avisos;
    }

    public int getSinLeer() {
        return sinLeer;
    }

    public Integer getRecienCreado() {
        return recienCreado;
    }
}
