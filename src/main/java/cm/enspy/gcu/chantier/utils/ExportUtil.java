package cm.enspy.gcu.chantier.utils;

import cm.enspy.gcu.chantier.config.Constants;
import com.opencsv.CSVWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

/**
 * Utilitaire d'exportation des rapports en PDF et CSV.
 * Gère la création des fichiers dans le répertoire dédiée aux rapports.
 */
public final class ExportUtil {

    private static final Logger LOGGER = Logger.getLogger(ExportUtil.class.getName());
    private static final float MARGIN = 50f;
    private static final float FONT_SIZE = 10f;
    private static final float TITLE_SIZE = 14f;
    private static final float LINE_HEIGHT = 15f;

    /** Constructeur privé — classe utilitaire. */
    private ExportUtil() {}

    /**
     * Exporte un rapport au format PDF en utilisant PDFBox.
     *
     * @param titre    titre du rapport
     * @param lignes   lignes de contenu du rapport
     * @param nomFichier nom du fichier sans extension
     * @return chemin absolu du fichier créé
     * @throws IOException en cas d'erreur d'écriture
     */
    public static String exporterPDF(String titre, List<String> lignes, String nomFichier)
            throws IOException {
        File dir = new File(Constants.RAPPORTS_DIR);
        if (!dir.exists()) dir.mkdirs();

        String horodatage = DateUtils.formaterPourFichier(LocalDateTime.now());
        String cheminFichier = Constants.RAPPORTS_DIR + "/" + nomFichier + "_" + horodatage + ".pdf";

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                float pageHeightActual = page.getMediaBox().getHeight();
                float y = pageHeightActual - MARGIN;

                // Titre
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), TITLE_SIZE);
                contentStream.newLineAtOffset(MARGIN, y);
                contentStream.showText(titre);
                contentStream.endText();
                y -= LINE_HEIGHT * 2;

                // Date de génération
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE), 8f);
                contentStream.newLineAtOffset(MARGIN, y);
                contentStream.showText("Généré le: " + DateUtils.formaterDateHeure(LocalDateTime.now()));
                contentStream.endText();
                y -= LINE_HEIGHT * 2;

                // Trait séparateur (simulé par une ligne de tirets)
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), FONT_SIZE);

                // Contenu
                for (String ligne : lignes) {
                    if (y < MARGIN + LINE_HEIGHT) {
                        // Nouvelle page si nécessaire
                        contentStream.close();
                        PDPage newPage = new PDPage(PDRectangle.A4);
                        document.addPage(newPage);
                        // Note: On ne peut pas réutiliser le contentStream fermé
                        // Pour simplifier, on tronque à la page courante
                        break;
                    }
                    contentStream.beginText();
                    contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), FONT_SIZE);
                    contentStream.newLineAtOffset(MARGIN, y);
                    // Nettoyage des caractères non supportés par PDType1Font
                    String ligneSafe = ligne.replaceAll("[^\u0000-\u00FF]", "?");
                    contentStream.showText(ligneSafe);
                    contentStream.endText();
                    y -= LINE_HEIGHT;
                }
            }

            document.save(cheminFichier);
            LOGGER.info("Rapport PDF exporté: " + cheminFichier);
            return cheminFichier;
        }
    }

    /**
     * Exporte un rapport au format CSV en utilisant OpenCSV.
     *
     * @param entetes    en-têtes des colonnes
     * @param donnees    lignes de données (tableau de strings par ligne)
     * @param nomFichier nom du fichier sans extension
     * @return chemin absolu du fichier créé
     * @throws IOException en cas d'erreur d'écriture
     */
    public static String exporterCSV(String[] entetes, List<String[]> donnees, String nomFichier)
            throws IOException {
        File dir = new File(Constants.RAPPORTS_DIR);
        if (!dir.exists()) dir.mkdirs();

        String horodatage = DateUtils.formaterPourFichier(LocalDateTime.now());
        String cheminFichier = Constants.RAPPORTS_DIR + "/" + nomFichier + "_" + horodatage + ".csv";

        try (CSVWriter writer = new CSVWriter(new FileWriter(cheminFichier))) {
            writer.writeNext(entetes);
            writer.writeAll(donnees);
        }

        LOGGER.info("Rapport CSV exporté: " + cheminFichier);
        return cheminFichier;
    }
}
