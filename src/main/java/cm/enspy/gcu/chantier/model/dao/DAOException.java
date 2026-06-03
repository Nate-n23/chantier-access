package cm.enspy.gcu.chantier.model.dao;

/**
 * Exception levée par les DAO en cas d'erreur d'accès aux données.
 * Encapsule les SqlExceptions et les rethrow comme RuntimeException.
 */
public class DAOException extends RuntimeException {

    /**
     * Constructeur avec message d'erreur.
     *
     * @param message message décrivant l'erreur
     */
    public DAOException(String message) {
        super(message);
    }

    /**
     * Constructeur avec message et cause.
     *
     * @param message message décrivant l'erreur
     * @param cause   exception SQL d'origine
     */
    public DAOException(String message, Throwable cause) {
        super(message, cause);
    }
}
