package cm.enspy.gcu.chantier;

/**
 * Classe de lancement — contournement nécessaire pour les fat JARs JavaFX.
 * Le plugin maven-shade ne peut pas détecter l'extension Application si MainApp
 * est directement déclaré comme Main-Class dans le manifest.
 */
public class Launcher {
    /**
     * Point d'entrée réel de l'application.
     *
     * @param args arguments de la ligne de commande
     */
    public static void main(String[] args) {
        MainApp.main(args);
    }
}
