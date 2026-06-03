package cm.enspy.gcu.chantier.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utilitaires de manipulation et formatage des dates.
 * Utilise exclusivement l'API java.time.*.
 */
public final class DateUtils {

    /** Formateur standard pour l'affichage des dates. */
    public static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /** Formateur standard pour l'affichage des dates et heures. */
    public static final DateTimeFormatter DATETIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    /** Formateur pour la génération de noms de fichiers. */
    public static final DateTimeFormatter FILENAME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    /** Constructeur privé — classe utilitaire. */
    private DateUtils() {}

    /**
     * Formate une date en chaîne lisible.
     *
     * @param date date à formater
     * @return chaîne formatée, ou chaîne vide si null
     */
    public static String formaterDate(LocalDate date) {
        if (date == null) return "";
        return date.format(DATE_FORMATTER);
    }

    /**
     * Formate une date et heure en chaîne lisible.
     *
     * @param dateHeure date et heure à formater
     * @return chaîne formatée, ou chaîne vide si null
     */
    public static String formaterDateHeure(LocalDateTime dateHeure) {
        if (dateHeure == null) return "";
        return dateHeure.format(DATETIME_FORMATTER);
    }

    /**
     * Formate une date pour usage dans un nom de fichier.
     *
     * @param dateHeure date et heure
     * @return chaîne formatée sans caractères spéciaux
     */
    public static String formaterPourFichier(LocalDateTime dateHeure) {
        if (dateHeure == null) return "unknown";
        return dateHeure.format(FILENAME_FORMATTER);
    }

    /**
     * Parse une chaîne au format ISO (yyyy-MM-dd) en LocalDate.
     *
     * @param dateStr chaîne de date
     * @return LocalDate parsée, ou null si null ou vide
     */
    public static LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return null;
        return LocalDate.parse(dateStr);
    }

    /**
     * Parse une chaîne au format ISO (yyyy-MM-dd HH:mm:ss) en LocalDateTime.
     *
     * @param dateTimeStr chaîne de date-heure
     * @return LocalDateTime parsée, ou null si null ou vide
     */
    public static LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isBlank()) return null;
        // SQLite retourne le format "yyyy-MM-dd HH:mm:ss"
        return LocalDateTime.parse(dateTimeStr.replace(" ", "T"));
    }
}
