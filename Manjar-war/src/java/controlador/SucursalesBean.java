package controlador;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.model.SelectItem;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import logicaneg.MSucursal;
import logicaneg.ReglaNegocioException;
import logicaneg.SucursalVista;
import modelo.Usuario;

/**
 * Sucursales del dueño (HU-18): registrar sucursales, asignarles un Gerente y elegir cuál operar.
 * Solo el Dueño entra a esta pantalla; MSucursal vuelve a comprobarlo en cada acción.
 */
@Named("sucursalesBean")
@ViewScoped
public class SucursalesBean implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String NUEVA = "NUEVA";
    public static final String EXISTENTE = "EXISTENTE";

    @EJB
    private MSucursal mSucursal;

    @Inject
    private SesionBean sesion;

    private List<SucursalVista> sucursales = new ArrayList<>();
    private List<Usuario> gerentes = new ArrayList<>();

    // Nueva sucursal
    private String nombre;
    private String direccion;

    // Asignar gerente (ventana)
    private SucursalVista seleccionada;
    private String modo = NUEVA;
    private String gerenteNombre;
    private String gerenteCorreo;
    private String gerenteClave;
    private Integer gerenteExistenteId;

    @PostConstruct
    public void init() {
        refrescar();
    }

    public void refrescar() {
        try {
            sucursales = mSucursal.listar(sesion.getUsuarioId());
            gerentes = mSucursal.gerentes(sesion.getUsuarioId());
        } catch (ReglaNegocioException e) {
            Mensajes.error(e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // Registrar sucursal
    // ------------------------------------------------------------------
    public void crear() {
        try {
            SucursalVista s = mSucursal.crear(sesion.getUsuarioId(), nombre, direccion);
            Mensajes.info("Sucursal \"" + s.getNombre() + "\" registrada. Asígnale un gerente o ábrela con tu cuenta de dueño.");
            nombre = null;
            direccion = null;
        } catch (ReglaNegocioException e) {
            Mensajes.error(e.getMessage());
        }
        refrescar();
    }

    // ------------------------------------------------------------------
    // Asignar gerente
    // ------------------------------------------------------------------
    public void abrirAsignar(SucursalVista s) {
        seleccionada = s;
        modo = NUEVA;
        gerenteNombre = null;
        gerenteCorreo = null;
        gerenteClave = null;
        gerenteExistenteId = null;
    }

    public void asignar() {
        if (seleccionada == null) {
            Mensajes.error("Elige una sucursal.");
            Mensajes.marcarFallo();
            return;
        }
        try {
            SucursalVista r;
            if (EXISTENTE.equals(modo)) {
                r = mSucursal.moverGerente(sesion.getUsuarioId(), gerenteExistenteId, seleccionada.getId());
            } else {
                r = mSucursal.crearGerente(sesion.getUsuarioId(), seleccionada.getId(), gerenteNombre, gerenteCorreo, gerenteClave);
            }
            Mensajes.info((r.getGerenteNombre() == null ? "El gerente" : r.getGerenteNombre())
                    + " quedó asignado a \"" + r.getNombre() + "\".");
            seleccionada = null;
        } catch (ReglaNegocioException e) {
            Mensajes.error(e.getMessage());
            Mensajes.marcarFallo(); // la ventana se queda abierta
        } finally {
            gerenteClave = null;
        }
        refrescar();
    }

    // ------------------------------------------------------------------
    // Operar otra sucursal
    // ------------------------------------------------------------------
    public String operar(Integer sucursalId) {
        try {
            SucursalVista s = mSucursal.operar(sesion.getUsuarioId(), sucursalId);
            sesion.cambiarSucursal(s.getId(), s.getNombre());
            Mensajes.info("Ahora operas \"" + s.getNombre() + "\".");
        } catch (ReglaNegocioException e) {
            Mensajes.error(e.getMessage());
        }
        refrescar();
        return null;
    }

    // ------------------------------------------------------------------
    // Para la vista
    // ------------------------------------------------------------------
    public boolean isActiva(SucursalVista s) {
        return s != null && s.getId() != null && s.getId().equals(sesion.getEstablecimientoId());
    }

    public boolean isUnaSola() {
        return sucursales.size() <= 1;
    }

    public boolean isHayGerentesParaMover() {
        if (seleccionada == null) {
            return false;
        }
        for (Usuario g : gerentes) {
            if (g.getEstablecimientoid() != null && !g.getEstablecimientoid().getId().equals(seleccionada.getId())) {
                return true;
            }
        }
        return false;
    }

    /** Gerentes activos de las otras sucursales (los que se pueden pasar a la sucursal elegida). */
    public List<SelectItem> getGerentesParaMover() {
        List<SelectItem> r = new ArrayList<>();
        if (seleccionada == null) {
            return r;
        }
        for (Usuario g : gerentes) {
            if (g.getEstablecimientoid() != null && !g.getEstablecimientoid().getId().equals(seleccionada.getId())) {
                r.add(new SelectItem(g.getId(), g.getNombre() + " (" + g.getEstablecimientoid().getNombre() + ")"));
            }
        }
        return r;
    }

    public List<SucursalVista> getSucursales() {
        return sucursales;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public SucursalVista getSeleccionada() {
        return seleccionada;
    }

    public String getModo() {
        return modo;
    }

    public void setModo(String modo) {
        this.modo = modo;
    }

    public String getGerenteNombre() {
        return gerenteNombre;
    }

    public void setGerenteNombre(String gerenteNombre) {
        this.gerenteNombre = gerenteNombre;
    }

    public String getGerenteCorreo() {
        return gerenteCorreo;
    }

    public void setGerenteCorreo(String gerenteCorreo) {
        this.gerenteCorreo = gerenteCorreo;
    }

    public String getGerenteClave() {
        return gerenteClave;
    }

    public void setGerenteClave(String gerenteClave) {
        this.gerenteClave = gerenteClave;
    }

    public Integer getGerenteExistenteId() {
        return gerenteExistenteId;
    }

    public void setGerenteExistenteId(Integer gerenteExistenteId) {
        this.gerenteExistenteId = gerenteExistenteId;
    }
}
