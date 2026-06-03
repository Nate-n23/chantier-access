package cm.enspy.gcu.chantier.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utilitaire de hachage cryptographique.
 * Fournit le hachage SHA-256 pour les mots de passe.
 */
public final class HashUtil {

    /** Constructeur privé — classe utilitaire. */
    private HashUtil() {}

    /**
     * Calcule le hachage SHA-256 d'une chaîne de caractères.
     *
     * @param input chaîne à hacher
     * @return représentation hexadécimale du hachage SHA-256
     * @throws IllegalStateException si l'algorithme SHA-256 n'est pas disponible
     */
    public static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Algorithme SHA-256 non disponible", e);
        }
    }

    /**
     * Vérifie si une chaîne correspond à un hachage SHA-256 donné.
     *
     * @param input     chaîne à vérifier
     * @param hashCible hachage de référence
     * @return true si le hachage correspond
     */
    public static boolean verifier(String input, String hashCible) {
        return sha256(input).equals(hashCible);
    }
}
