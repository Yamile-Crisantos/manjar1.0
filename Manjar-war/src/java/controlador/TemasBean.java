package controlador;

import java.util.List;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import logicaneg.Temas;

/** Colores y estilos que un negocio puede elegir para su menú (los usan inicio.xhtml y negocio.xhtml). */
@Named("temasBean")
@RequestScoped
public class TemasBean {

    public List<Temas.Tema> getLista() {
        return Temas.todos();
    }

    public List<Temas.Opcion> getDisenos() {
        return Temas.disenos();
    }

    public List<Temas.Opcion> getTipografias() {
        return Temas.tipografias();
    }

    public List<Temas.Opcion> getEsquinas() {
        return Temas.esquinas();
    }

    public List<Temas.Opcion> getFondos() {
        return Temas.fondos();
    }
}
