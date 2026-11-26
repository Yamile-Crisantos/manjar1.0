package controlador;

import java.util.ArrayList;
import java.util.List;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import logicaneg.MApartado;
import logicaneg.MEstablecimiento;
import logicaneg.NegocioVista;
import logicaneg.ReglaNegocioException;
import modelo.Establecimiento;

/**
 * Cómo llega un cliente a un negocio sin volver a escanear su QR:
 *  - "Mis negocios": los negocios donde ya apartó (no es un directorio de la plataforma);
 *  - escribir (o pegar) el código del negocio o el enlace de su menú.
 * Lo usan el panel del cliente y la pantalla de apartar.
 */
@Named("negocioCliente")
@RequestScoped
public class NegocioClienteBean {

    @EJB
    private MApartado mApartado;

    @EJB
    private MEstablecimiento mEstablecimiento;

    @Inject
    private SesionBean sesion;

    /** Lo que escribió el cliente: el código del negocio o el enlace de su menú. */
    private String codigo;
    private List<NegocioVista> mis;

    public List<NegocioVista> getMis() {
        if (mis == null) {
            mis = new ArrayList<>();
            if (sesion.isCliente()) {
                try {
                    mis = mApartado.negociosDelCliente(sesion.getUsuarioId());
                } catch (ReglaNegocioException e) {
                    // sin lista: el cliente todavía puede escribir el código
                }
            }
        }
        return mis;
    }

    /** Abre la pantalla de apartar en el negocio del código escrito. */
    public String irAlNegocio() {
        String q = extraerCodigo(codigo);
        if (q == null) {
            Mensajes.error("Escribe el código del negocio. Lo encuentras en su menú o en su QR; también sirve pegar el enlace de su menú.");
            return null;
        }
        Establecimiento e = mEstablecimiento.buscarPorCodigoQR(q);
        if (e == null) {
            Mensajes.error("No encontramos un negocio con ese código. Revísalo e inténtalo de nuevo.");
            return null;
        }
        return "/vistas/apartar?faces-redirect=true&q=" + q;
    }

    /**
     * Saca el código de lo que escribió el cliente: el código solo ("fonda-lupe-7k3m2q") o un enlace completo del
     * menú (".../menu.xhtml?q=fonda-lupe-7k3m2q"). Devuelve null si no tiene el formato de un código.
     */
    static String extraerCodigo(String texto) {
        if (texto == null) {
            return null;
        }
        String t = texto.trim().toLowerCase();
        int i = t.indexOf("q=");
        if (i >= 0) {
            t = t.substring(i + 2);
            int fin = 0;
            while (fin < t.length() && !"&# \t\r\n".contains(String.valueOf(t.charAt(fin)))) {
                fin++;
            }
            t = t.substring(0, fin);
        }
        return MEstablecimiento.codigoConFormato(t) ? t : null;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }
}
