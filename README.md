# Chantier Accès — Logiciel de Gestion des Accès Chantier

## 🏗️ Présentation du Projet
Ce logiciel est une solution complète de gestion et de contrôle des accès pour les chantiers de construction. Développé en Java 17 avec JavaFX, il permet de sécuriser le périmètre du chantier, de suivre les intervenants en temps réel et d'assurer le respect des normes de sécurité (EPI, habilitations).

## 🚀 Fonctionnalités Clés
- **Contrôle d'Accès :** Validation des badges par zone, gestion des entrées/sorties et détection des accès non autorisés.
- **Gestion des Intervenants :** Fiches complètes avec matricule, entreprise, photo et niveau d'habilitation.
- **Gestion des Badges :** Attribution de badges permanents ou temporaires avec encodage automatique.
- **Suivi des EPI :** Attribution et contrôle de l'état des équipements de protection individuelle (Casque, Gilet, etc.).
- **Tableau de Bord :** KPIs en temps réel, graphique de fréquentation et alertes de sécurité.
- **Rapports :** Génération de rapports d'activité en formats PDF et CSV.
- **Administration :** Gestion des comptes utilisateurs (Rôles: Admin, Responsable, Agent, Consultant) et journal d'audit complet.

## 🛠️ Stack Technique
- **Langage :** Java 17 (Amazon Corretto recommandé)
- **UI :** JavaFX 17 (Stylisation via Vanilla CSS moderne)
- **Base de Données :** SQLite (JDBC)
- **Build :** Maven 3.9+
- **Tests :** JUnit 5
- **Librairies :** PDFBox, OpenCSV, SQLite-JDBC

## 📦 Compilation et Exécution

### Prérequis
- Java 17+ installé
- Maven installé

### Compilation
Pour compiler le projet et générer le Fat JAR :
```bash
mvn clean package
```

### Exécution
Une fois compilé, lancez l'application via :
```bash
java -jar target/chantier-acces-1.0.jar
```

**Identifiants par défaut (Administrateur) :**
- **Login :** admin
- **Mot de passe :** Admin123

## 📂 Structure du Projet
- `src/main/java` : Code source Java (Architecture Layered : DAO, Service, Controller, Entity).
- `src/main/resources` : Fichiers FXML (Vues), CSS (Styles), SQL (Initialisation) et Configuration.
- `src/test/java` : Tests unitaires et d'intégration.
- `target/` : Dossier de build contenant le JAR exécutable.

## ⚖️ License
Projet réalisé dans le cadre du cours de Génie Civil et Urbain (ENSPY).
