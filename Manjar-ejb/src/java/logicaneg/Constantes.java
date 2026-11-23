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

    // ------------------------------------------------------------------
    // Comandas (HU-08, HU-09, HU-10)
    // ------------------------------------------------------------------
    public static final String TIPO_MESA = "MESA";
    public static final String TIPO_LLEVAR = "LLEVAR";

    public static final String COMANDA_PENDIENTE = "PENDIENTE";
    public static final String COMANDA_EN_PREPARACION = "EN_PREPARACION";
    public static final String COMANDA_LISTA = "LISTA";
    /** La llevaron a la mesa (o la recogieron). Ya no estorba en la pantalla de cocina. */
    public static final String COMANDA_ENTREGADA = "ENTREGADA";
    public static final String COMANDA_CANCELADA = "CANCELADA";

    // ------------------------------------------------------------------
    // Caja (HU-11, HU-12)
    // ------------------------------------------------------------------
    public static final String CAJA_ABIERTA = "ABIERTA";
    public static final String CAJA_CERRADA = "CERRADA";

    public static final String MOV_INGRESO = "INGRESO";
    public static final String MOV_EGRESO = "EGRESO";

    public static final String CLASE_VENTA = "VENTA";
    public static final String CLASE_APARTADO = "APARTADO";
    public static final String CLASE_RETIRO = "RETIRO";
    public static final String CLASE_GASTO = "GASTO";

    public static final String METODO_EFECTIVO = "EFECTIVO";
    public static final String METODO_TARJETA = "TARJETA";
    public static final String METODO_TRANSFERENCIA = "TRANSFERENCIA";

    // ------------------------------------------------------------------
    // Apartados (HU-13 a HU-17)
    // ------------------------------------------------------------------
    /** Porciones retenidas, esperando el pago del anticipo. */
    public static final String APARTADO_PENDIENTE = "PENDIENTE";
    public static final String APARTADO_PAGADO = "PAGADO";
    public static final String APARTADO_LISTO = "LISTO";
    public static final String APARTADO_ENTREGADO = "ENTREGADO";
    public static final String APARTADO_CANCELADO = "CANCELADO";
    /** No se pagó a tiempo: las porciones regresaron al menú. */
    public static final String APARTADO_VENCIDO = "VENCIDO";

    public static final String PAGO_CONFIRMADO = "CONFIRMADO";

    public static final String NOTIF_NUEVA = "PENDIENTE";
    public static final String NOTIF_LEIDA = "LEIDA";
    public static final String NOTIF_PAGO = "PAGO_RECIBIDO";
    public static final String NOTIF_LISTO = "PEDIDO_LISTO";
    public static final String NOTIF_CANCELADO = "APARTADO_CANCELADO";
    public static final String NOTIF_VENCIDO = "APARTADO_VENCIDO";

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
