package cm.enspy.gcu.chantier;

import cm.enspy.gcu.chantier.model.entities.Badge;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour l'entité Badge.
 * Vérifient la logique de validité et d'expiration.
 */
class BadgeTest {

    /**
     * Test 1: Un badge actif et non expiré doit retourner true pour isValide().
     */
    @Test
    void isValide_badgeActifNonExpire_returnsTrue() {
        Badge badge = new Badge();
        badge.setCode("TEST0001");
        badge.setEstActif(true);
        badge.setDateExpiration(LocalDate.now().plusDays(30));
        badge.setTypeBadge(Badge.TypeBadge.PERMANENT);
        badge.setDateCreation(LocalDateTime.now());

        assertTrue(badge.isValide(), "Un badge actif avec date d'expiration future doit être valide");
    }

    /**
     * Test 2: Un badge avec une date d'expiration passée doit retourner false pour isValide().
     */
    @Test
    void isValide_badgeExpire_returnsFalse() {
        Badge badge = new Badge();
        badge.setCode("TEST0002");
        badge.setEstActif(true);
        badge.setDateExpiration(LocalDate.now().minusDays(1));
        badge.setTypeBadge(Badge.TypeBadge.TEMPORAIRE);
        badge.setDateCreation(LocalDateTime.now().minusDays(10));

        assertFalse(badge.isValide(), "Un badge avec date d'expiration passée ne doit pas être valide");
    }
}
