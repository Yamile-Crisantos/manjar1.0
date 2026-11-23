package controlador;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import logicaneg.ComandaVista;
import logicaneg.Constantes;
import logicaneg.MComanda;
import logicaneg.MEstablecimiento;
import logicaneg.ReglaNegocioException;
import modelo.Establecimiento;

/**
 * Pantalla compartida de cocina (HU-10).
 *
 * No pide cuenta de cocina: entra quien tenga la clave de la sucursal (enlace con ?k=<clave>) o
 * cualquier persona del personal con sesión iniciada (desde su panel). La clave se vuelve a
 * comprobar en CADA petición, así que si el dueño o el gerente generan una nueva, el enlace
 * anterior deja de funcionar al instante. Con la clave solo se pueden ver las comandas de esa
 * sucursal y cambiar su estado: no da acceso a nada más.
 */
@Named("cocinaBean")
@ViewScoped
public class CocinaBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String CLAVE_PETICION = "manjar.cocina.sucursal";

    @EJB
    private MComanda mComanda;

    @EJB
    private MEstablecimiento mEstablecimiento;

    @Inject
    private SesionBean sesion;

    /** La clave que llegó en la dirección (?k=...). */
    private String token;

    private List<ComandaVista> pendientes = new ArrayList<>();
    private List<ComandaVista> enPreparacion = new ArrayList<>();
    private List<ComandaVista> listas = new ArrayList<>();
    private boolean cargado;
    private boolean tuvoAcceso;

    // ------------------------------------------------------------------
    // Quién puede ver la pantalla
    // ------------------------------------------------------------------
    /** La sucursal de esta petición: por la clave del enlace, o por la sesión del personal. Null si no hay acceso. */
    private Establecimiento sucursal() {
        Map<String, Object> peticion = FacesContext.getCurrentInstance().getExternalContext().getRequestMap();
        if (peticion.containsKey(CLAVE_PETICION)) {
            return (Establecimiento) peticion.get(CLAVE_PETICION);
        }
        Establecimiento e = null;
        if (MEstablecimiento.tokenCocinaConFormato(token)) {
            e = mEstablecimiento.buscarPorTokenCocina(token);
        }
        if (e == null && sesion.isPersonal() && sesion.getEstablecimientoId() != null) {
            e = mEstablecimiento.buscar(sesion.getEstablecimientoId());
        }
        peticion.put(CLAVE_PETICION, e);
        return e;
    }

    public boolean isAutorizado() {
        boolean autorizado = sucursal() != null;
        if (autorizado) {
            tuvoAcceso = true;
        }
        return autorizado;
    }

    /**
     * true si esta pantalla ya estuvo abierta con acceso y ahora lo perdió (cambiaron la clave o se cerró la sesión).
     * Se guarda en el bean de la vista para que el sondeo automático pueda avisarlo en vez de dejar la pantalla vacía.
     */
    public boolean isPerdioAcceso() {
        return tuvoAcceso && !isAutorizado();
    }

    public String getSucursalNombre() {
        Establecimiento e = sucursal();
        return e == null ? "" : e.getNombre();
    }

    /** true si entró con la clave del enlace y sin cuenta (pantalla compartida). */
    public boolean isCompartida() {
        return !sesion.isPersonal() && isAutorizado();
    }

    // ------------------------------------------------------------------
    // Lectura
    // ------------------------------------------------------------------
    public void refrescar() {
        Establecimiento e = sucursal();
        pendientes = new ArrayList<>();
        enPreparacion = new ArrayList<>();
        listas = new ArrayList<>();
        cargado = true;
        if (e == null) {
            return;
        }
        for (ComandaVista c : mComanda.tablero(e.getId())) {
            if (c.isPendiente()) {
                pendientes.add(c);
            } else if (c.isEnPreparacion()) {
                enPreparacion.add(c);
            } else if (c.isLista()) {
                listas.add(c);
            }
        }
    }

    private void asegurarCarga() {
        if (!cargado) {
            refrescar();
        }
    }

    public List<ComandaVista> getPendientes() {
        asegurarCarga();
        return pendientes;
    }

    public List<ComandaVista> getEnPreparacion() {
        asegurarCarga();
        return enPreparacion;
    }

    public List<ComandaVista> getListas() {
        asegurarCarga();
        return listas;
    }

    public int getTotal() {
        asegurarCarga();
        return pendientes.size() + enPreparacion.size() + listas.size();
    }

    // ------------------------------------------------------------------
    // Cambiar el estado
    // ------------------------------------------------------------------
    public void empezar(Integer comandaId) {
        cambiar(comandaId, Constantes.COMANDA_EN_PREPARACION);
    }

    public void terminar(Integer comandaId) {
        cambiar(comandaId, Constantes.COMANDA_LISTA);
    }

    public void regresarAPendiente(Integer comandaId) {
        cambiar(comandaId, Constantes.COMANDA_PENDIENTE);
    }

    public void regresarAPreparacion(Integer comandaId) {
        cambiar(comandaId, Constantes.COMANDA_EN_PREPARACION);
    }

    private void cambiar(Integer comandaId, String destino) {
        Establecimiento e = sucursal();
        if (e == null) {
            Mensajes.error("Esta pantalla ya no tiene acceso. Abre de nuevo el enlace de la cocina.");
            return;
        }
        try {
            mComanda.cambiarEstadoCocina(e.getId(), comandaId, destino);
        } catch (ReglaNegocioException ex) {
            Mensajes.error(ex.getMessage());
        }
        refrescar();
    }

    // ------------------------------------------------------------------
    // Enlace de la pantalla compartida (solo dueño y gerente)
    // ------------------------------------------------------------------
    public boolean isPuedeAdministrar() {
        return sesion.isAdministra() && sesion.getEstablecimientoId() != null;
    }

    public String getEnlaceCompartido() {
        if (!isPuedeAdministrar()) {
            return "";
        }
        try {
            return Enlaces.cocinaCompleto(mEstablecimiento.tokenCocina(sesion.getEstablecimientoId(), sesion.getUsuarioId()));
        } catch (ReglaNegocioException e) {
            return "";
        }
    }

    public void generarNuevaClave() {
        if (!isPuedeAdministrar()) {
            return;
        }
        try {
            mEstablecimiento.regenerarTokenCocina(sesion.getEstablecimientoId(), sesion.getUsuarioId());
            Mensajes.info("Se generó una clave nueva. El enlace anterior ya no funciona.");
        } catch (ReglaNegocioException e) {
            Mensajes.error(e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
        // El parámetro puede llegar después de que alguien ya preguntó por la sucursal: se empieza de nuevo
        FacesContext.getCurrentInstance().getExternalContext().getRequestMap().remove(CLAVE_PETICION);
        cargado = false;
    }
}
