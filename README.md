# Application de gestion des dispenses

Application web permettant aux étudiants d'introduire une demande de dispense auprès de l'ISFCE sur base de cours déjà réussis dans d'autres établissements.

## Fonctionnalités

- Consultation des UE de l'ISFCE par section
- Saisie des cours réussis dans d'autres écoles (avec auto-reconnaissance)
- Demandes de dispense avec association des cours justificatifs
- Upload de documents (bulletin, lettre de motivation, programmes de cours en PDF)
- Suivi du statut des dossiers (en cours, en traitement, clôturé)
- Authentification sécurisée via Keycloak (OAuth2 / JWT)

## Stack technique

**Backend**
- Spring Boot 3.5
- Java 21
- Hibernate / JPA
- H2 (base de données)
- Keycloak (authentification)
- MapStruct (mapping DTO)
- Lombok
- Swagger UI

**Frontend**
- Angular 21 (standalone, signals)
- Angular Material 3
- keycloak-angular

## Lancement en local

### Prérequis
- Java 21
- Node.js + npm
- Keycloak (port 8084, realm `DISP`, client `etu-app`)

### Backend

```bash
./gradlew bootRun
```

Le backend démarre sur `http://localhost:9090`.

### Frontend

```bash
cd dispense_app_ang
npm install
npx ng serve
```

Le frontend démarre sur `http://localhost:4200`.

### Build de production (tout en un JAR)

```bash
./gradlew clean bootJar
```

Le frontend est automatiquement compilé et inclus dans le JAR final
(`build/libs/projetWebPid2526Ludo-v0.1.jar`).

Pour lancer avec le profil de production :

```bash
java -jar projetWebPid2526Ludo-v0.1.jar --spring.profiles.active=prod
```

## Endpoints utiles

- API REST : `http://localhost:9090/api/**`
- Swagger UI : `http://localhost:9090/swagger-ui.html`
- Console H2 : `http://localhost:9090/h2`
- Actuator : `http://localhost:9090/actuator/health`

## Architecture

```
Frontend (Angular)  ──HTTP/JSON──►  Backend (Spring Boot)  ──SQL──►  H2
                                          │
                    ◄──JWT token──  Keycloak
```

Architecture backend en couches :

- `controller/` — endpoints REST, sécurité par rôle
- `service/` — logique métier, transactions
- `dao/` — accès base de données (Spring Data JPA)
- `model/` — entités JPA
- `dto/` — objets de transfert vers le frontend
- `mapper/` — conversion entité ↔ DTO (MapStruct)
- `exception/` — exceptions métier
- `config/` — sécurité, CORS, Swagger

## Tests

132 tests (DAO, Service, Controller).

```bash
./gradlew test
```
