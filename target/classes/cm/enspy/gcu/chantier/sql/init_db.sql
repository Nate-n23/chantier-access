-- ============================================================
-- Script d'initialisation de la base de données Chantier Accès
-- Exécuté automatiquement au premier lancement
-- ============================================================

-- Table des intervenants
CREATE TABLE IF NOT EXISTS intervenants (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    matricule TEXT UNIQUE NOT NULL,
    nom TEXT NOT NULL,
    prenom TEXT NOT NULL,
    date_naissance TEXT,
    telephone TEXT,
    email TEXT,
    entreprise TEXT,
    categorie TEXT CHECK(categorie IN ('OUVRIER','TECHNICIEN','INGENIEUR','VISITEUR','SOUS_TRAITANT')),
    statut TEXT CHECK(statut IN ('ACTIF','SUSPENDU','EXPULSE','ARCHIVE')) DEFAULT 'ACTIF',
    photo_path TEXT,
    niveau_habilitation INTEGER DEFAULT 1,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table des badges
CREATE TABLE IF NOT EXISTS badges (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    code TEXT UNIQUE NOT NULL,
    type_badge TEXT CHECK(type_badge IN ('PERMANENT','TEMPORAIRE')),
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_expiration TEXT,
    est_actif INTEGER DEFAULT 1,
    intervenant_id INTEGER,
    FOREIGN KEY (intervenant_id) REFERENCES intervenants(id)
);

-- Table des zones
CREATE TABLE IF NOT EXISTS zones (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nom TEXT NOT NULL,
    description TEXT,
    niveau_securite INTEGER DEFAULT 1 CHECK(niveau_securite BETWEEN 1 AND 4),
    capacite_max INTEGER DEFAULT 0,
    est_active INTEGER DEFAULT 1
);

-- Table d'association badges <-> zones autorisées
CREATE TABLE IF NOT EXISTS badges_zones (
    badge_id INTEGER NOT NULL,
    zone_id INTEGER NOT NULL,
    PRIMARY KEY (badge_id, zone_id),
    FOREIGN KEY (badge_id) REFERENCES badges(id),
    FOREIGN KEY (zone_id) REFERENCES zones(id)
);

-- Table des enregistrements d'accès
CREATE TABLE IF NOT EXISTS acces_zones (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    intervenant_id INTEGER,
    badge_id INTEGER,
    zone_id INTEGER,
    date_heure TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    type TEXT CHECK(type IN ('ENTREE','SORTIE')),
    statut TEXT CHECK(statut IN ('AUTORISE','REFUSE')),
    motif_refus TEXT,
    agent_id INTEGER,
    FOREIGN KEY (intervenant_id) REFERENCES intervenants(id),
    FOREIGN KEY (badge_id) REFERENCES badges(id),
    FOREIGN KEY (zone_id) REFERENCES zones(id)
);

-- Table des EPI
CREATE TABLE IF NOT EXISTS epi (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    type TEXT CHECK(type IN ('CASQUE','GILET','CHAUSSURES','HARNAIS','LUNETTES','GANTS')),
    taille TEXT,
    etat TEXT CHECK(etat IN ('BON','USAGE','A_REMPLACER')) DEFAULT 'BON',
    date_attribution TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    intervenant_id INTEGER,
    FOREIGN KEY (intervenant_id) REFERENCES intervenants(id)
);

-- Table des utilisateurs système
CREATE TABLE IF NOT EXISTS utilisateurs_systeme (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    login TEXT UNIQUE NOT NULL,
    mot_de_passe_hash TEXT NOT NULL,
    role TEXT CHECK(role IN ('ADMIN','RESPONSABLE','AGENT','CONSULTANT')),
    est_actif INTEGER DEFAULT 1,
    derniere_connexion TIMESTAMP
);

-- Table des alertes de sécurité
CREATE TABLE IF NOT EXISTS alertes_securite (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    type TEXT,
    message TEXT,
    date_heure TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    est_lue INTEGER DEFAULT 0,
    niveau_gravite TEXT CHECK(niveau_gravite IN ('INFO','AVERTISSEMENT','CRITIQUE')),
    source_id INTEGER
);

-- Table du journal d'audit
CREATE TABLE IF NOT EXISTS journal_audit (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    utilisateur_id INTEGER,
    action TEXT,
    table_ciblee TEXT,
    date_heure TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ancienne_valeur TEXT,
    nouvelle_valeur TEXT,
    ip_machine TEXT,
    FOREIGN KEY (utilisateur_id) REFERENCES utilisateurs_systeme(id)
);

-- ============================================================
-- Données initiales
-- ============================================================

-- Administrateur par défaut: login=admin, mot de passe=Admin123 (SHA-256)
-- SHA-256("Admin123") = a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3
INSERT OR IGNORE INTO utilisateurs_systeme (login, mot_de_passe_hash, role, est_actif)
VALUES ('admin', 'a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3', 'ADMIN', 1);

-- Zones de démonstration
INSERT OR IGNORE INTO zones (id, nom, description, niveau_securite, capacite_max, est_active)
VALUES (1, 'Entrée principale', 'Zone d''accueil et de contrôle à l''entrée du chantier', 1, 100, 1);

INSERT OR IGNORE INTO zones (id, nom, description, niveau_securite, capacite_max, est_active)
VALUES (2, 'Zone technique', 'Zone des équipements et installations techniques', 3, 20, 1);

INSERT OR IGNORE INTO zones (id, nom, description, niveau_securite, capacite_max, est_active)
VALUES (3, 'Zone direction', 'Bureaux et salle de réunion de la direction', 4, 10, 1)
