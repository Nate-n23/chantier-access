package cm.enspy.gcu.chantier.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Configuration de l'application chargée depuis config.properties.
 * Singleton thread-safe utilisant le pattern double-checked locking.
 */
public class AppConfig {

    private static final Logger LOGGER = Logger.getLogger(AppConfig.class.getName());
    private static volatile AppConfig instance;
    private final Properties props = new Properties();

    /**
     * Constructeur privé — charge les propriétés depuis le fichier de configuration.
     */
    private AppConfig() {
        try (InputStream is = AppConfig.class.getResourceAsStream(
                "/cm/enspy/gcu/chantier/config.properties")) {
            if (is != null) {
                props.load(is);
            }
        } catch (IOException e) {
            LOGGER.warning("Impossible de charger config.properties: " + e.getMessage());
        }
    }

    /**
     * Retourne l'instance unique de la configuration.
     *
     * @return instance AppConfig
     */
    public static AppConfig getInstance() {
        if (instance == null) {
            synchronized (AppConfig.class) {
                if (instance == null) {
                    instance = new AppConfig();
                }
            }
        }
        return instance;
    }

    /**
     * Retourne le nom du chantier configuré.
     *
     * @return nom du chantier
     */
    public String getNomChantier() {
        return props.getProperty("chantier.nom", "Chantier Principal");
    }

    /**
     * Retourne l'adresse du chantier configurée.
     *
     * @return adresse du chantier
     */
    public String getAdresseChantier() {
        return props.getProperty("chantier.adresse", "Adresse non configurée");
    }

    /**
     * Définit le nom du chantier et persiste la modification.
     *
     * @param nom nouveau nom du chantier
     */
    public void setNomChantier(String nom) {
        props.setProperty("chantier.nom", nom);
    }

    /**
     * Définit l'adresse du chantier et persiste la modification.
     *
     * @param adresse nouvelle adresse du chantier
     */
    public void setAdresseChantier(String adresse) {
        props.setProperty("chantier.adresse", adresse);
    }

    /**
     * Retourne une propriété par sa clé.
     *
     * @param key         clé de la propriété
     * @param defaultValue valeur par défaut si la clé est absente
     * @return valeur de la propriété
     */
    public String getProperty(String key, String defaultValue) {
        return props.getProperty(key, defaultValue);
    }
}
