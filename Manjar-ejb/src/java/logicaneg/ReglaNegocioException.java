package logicaneg;

import javax.ejb.ApplicationException;

/**
 * Se lanza cuando algo incumple una regla del negocio (correo repetido,
 * contraseña muy corta, platillo de otro negocio, etc.).
 *
 * El mensaje ya viene redactado para el usuario: la capa web solo lo muestra.
 * Con rollback = true, si se lanza a mitad de una operación se deshacen los
 * cambios de esa transacción.
 */
@ApplicationException(rollback = true)
public class ReglaNegocioException extends Exception {

    private static final long serialVersionUID = 1L;

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
