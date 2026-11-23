package controlador;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

/**
 * Fechas y horas para las pantallas, en la hora del servidor (la del negocio) y en español.
 * Se usa desde las vistas como #{fmt.fechaHora(x.fecha)}; si la fecha es null devuelve una raya.
 * (No se usa f:convertDateTime porque por omisión muestra la hora en UTC.)
 */
@Named("fmt")
@ApplicationScoped
public class FormatoBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Locale ES = new Locale("es", "MX");

    public String fechaHora(Date d) {
        return d == null ? "—" : new SimpleDateFormat("dd/MM/yyyy HH:mm", ES).format(d);
    }

    public String fecha(Date d) {
        return d == null ? "—" : new SimpleDateFormat("dd/MM/yyyy", ES).format(d);
    }

    public String hora(Date d) {
        return d == null ? "—" : new SimpleDateFormat("HH:mm", ES).format(d);
    }

    /** "dd/MM HH:mm": para listas del día donde el año sobra. */
    public String diaHora(Date d) {
        return d == null ? "—" : new SimpleDateFormat("dd/MM HH:mm", ES).format(d);
    }
}
