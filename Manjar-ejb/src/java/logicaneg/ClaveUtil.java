package logicaneg;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Manejo de contraseñas: nunca se guarda la contraseña, solo un "hash".
 *
 * Formato guardado en Usuario.Contrasenahash:   iteraciones:sal:hash
 *   - sal:  16 bytes aleatorios en hexadecimal (distinta por usuario)
 *   - hash: PBKDF2 con HMAC-SHA256 (viene incluido en el JDK)
 *
 * Ejemplo: 65000:9f2c...(32 hex):a41b...(64 hex)
 */
public final class ClaveUtil {

    private static final int ITERACIONES = 65000;
    private static final int BITS_HASH = 256;
    private static final int BYTES_SAL = 16;
    private static final SecureRandom ALEATORIO = new SecureRandom();

    private ClaveUtil() {
    }

    /** Genera el texto que se guarda en la base de datos para una contraseña nueva. */
    public static String generarHash(String clave) {
        byte[] sal = new byte[BYTES_SAL];
        ALEATORIO.nextBytes(sal);
        byte[] hash = derivar(clave, sal, ITERACIONES, BITS_HASH);
        return ITERACIONES + ":" + aHex(sal) + ":" + aHex(hash);
    }

    /** Compara la contraseña escrita con el hash guardado. */
    public static boolean verificar(String clave, String guardado) {
        if (clave == null || guardado == null) {
            return false;
        }
        String[] partes = guardado.split(":");
        if (partes.length != 3) {
            return false;
        }
        try {
            int iteraciones = Integer.parseInt(partes[0]);
            if (iteraciones < 1000 || iteraciones > 1000000) {
                return false;
            }
            byte[] sal = desdeHex(partes[1]);
            byte[] esperado = desdeHex(partes[2]);
            byte[] calculado = derivar(clave, sal, iteraciones, esperado.length * 8);
            // isEqual tarda lo mismo aunque difieran en el primer byte
            return MessageDigest.isEqual(esperado, calculado);
        } catch (RuntimeException e) {
            return false;
        }
    }

    private static byte[] derivar(String clave, byte[] sal, int iteraciones, int bits) {
        try {
            SecretKeyFactory fabrica = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            PBEKeySpec spec = new PBEKeySpec(clave.toCharArray(), sal, iteraciones, bits);
            return fabrica.generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("No se pudo calcular el hash de la contraseña", e);
        }
    }

    private static String aHex(byte[] datos) {
        StringBuilder sb = new StringBuilder(datos.length * 2);
        for (byte b : datos) {
            sb.append(Character.forDigit((b >> 4) & 0xF, 16));
            sb.append(Character.forDigit(b & 0xF, 16));
        }
        return sb.toString();
    }

    private static byte[] desdeHex(String hex) {
        if (hex.length() % 2 != 0) {
            throw new IllegalArgumentException("Hexadecimal inválido");
        }
        byte[] datos = new byte[hex.length() / 2];
        for (int i = 0; i < datos.length; i++) {
            int alto = Character.digit(hex.charAt(2 * i), 16);
            int bajo = Character.digit(hex.charAt(2 * i + 1), 16);
            if (alto < 0 || bajo < 0) {
                throw new IllegalArgumentException("Hexadecimal inválido");
            }
            datos[i] = (byte) ((alto << 4) | bajo);
        }
        return datos;
    }
}
