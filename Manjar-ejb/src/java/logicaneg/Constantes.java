package logicaneg;

/**
 * Códigos que usamos en la base de datos para roles y estados.
 *
 * El modelo guarda Rol y Estado como números (Integer). Para no escribir
 * "magic numbers" por todo el código, el equipo acordó esta convención:
 *
 *   Rol:    1 = Cliente, 2 = Dueño, 3 = Gerente, 4 = Mesero
 *   Estado: 1 = activo,  0 = inactivo
 *   Platillo.Estado: 1 = disponible, 0 = agotado
 *
 * (La cocina no tiene rol: usa una pantalla compartida sin cuenta.)
 */
public final class Constantes {

    private Constantes() {
    }

    public static final int ROL_CLIENTE = 1;
    public static final int ROL_DUENO = 2;
    public static final int ROL_GERENTE = 3;
    public static final int ROL_MESERO = 4;

    public static final int ESTADO_ACTIVO = 1;
    public static final int ESTADO_INACTIVO = 0;

    public static final int PLATILLO_DISPONIBLE = 1;
    public static final int PLATILLO_AGOTADO = 0;

    /** Nombre legible del rol, para mostrarlo en pantalla. */
    public static String nombreRol(Integer rol) {
        if (rol == null) {
            return "Visitante";
        }
        switch (rol) {
            case ROL_CLIENTE:
                return "Cliente";
            case ROL_DUENO:
                return "Dueño";
            case ROL_GERENTE:
                return "Gerente";
            case ROL_MESERO:
                return "Mesero";
            default:
                return "Sin rol";
        }
    }
}
