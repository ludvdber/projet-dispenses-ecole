-- === RESET IDENTITY COUNTERS ===
-- Nécessaire pour garantir des IDs prévisibles entre les méthodes de test
-- (@Transactional rollback ne réinitialise PAS les compteurs IDENTITY en H2)
ALTER TABLE TDOSSIER ALTER COLUMN ID RESTART WITH 100;
ALTER TABLE TCORRESPONDANCE ALTER COLUMN ID RESTART WITH 100;
ALTER TABLE TCORR_COURS ALTER COLUMN ID RESTART WITH 100;
ALTER TABLE TCOURS_ETUDIANT ALTER COLUMN ID RESTART WITH 100;
ALTER TABLE TDOCUMENT ALTER COLUMN ID RESTART WITH 100;
ALTER TABLE TDISPENSE ALTER COLUMN ID RESTART WITH 100;

--sections
merge into TSECTION(CODE,NOM) VALUES ('INFO','Bachelier en Informatique Orientation: Développement d''applications');
merge into TSECTION(CODE,NOM) VALUES ('COMPTA','Bachelier en Comptabilité');
merge into TSECTION(CODE,NOM) VALUES ('MARK','Bachelier en Marketing');
merge into TSECTION(CODE,NOM) VALUES ('SECR','Bachelier en Assistant de direction');

--UE et ses acquis
--IPAP
merge into TUE(CODE,ECTS,NB_PERIODES,NOM,REF,PRGM) values('IPAP',8,120,'PRINCIPES ALGORITHMIQUES ET PROGRAMMATION','752105U32D3',
cast('* d''identifier différents langages de programmation existants ;
 * de mettre en oeuvre une méthodologie de résolution de problème (observation,résolution, expérimentation, validation) et de la justifier en fonction de l''objectif
poursuivi ;
 * de concevoir, construire et représenter des algorithmes, en utilisant :
    o les types de données élémentaires,
    o les figures algorithmiques de base (séquence, alternative et répétitive),
    o les instructions,
    o les portées des variables,
    o les fonctions et procédures,
    o la récursivité,
    o les entrées/sorties,
    o les fichiers,
    o les structures de données de base (tableaux et enregistrements) ;
 * de traduire de manière adéquate des algorithmes en respectant les spécificités du langage utilisé (JAVA, PYTHON);
 * de documenter de manière complète et précise les programmes développés ;
 * de produire des tests pour valider les programmes développés.'as CLOB));

merge into TACQUIS(FKUE,NUM,ACQUIS,POURCENTAGE) values ('IPAP',1,'mettre en oeuvre une représentation algorithmique du problème posé',30);
merge into TACQUIS(FKUE,NUM,ACQUIS,POURCENTAGE) values ('IPAP',2,'de développer au moins un programme en respectant les spécificités du langage choisi',30);
merge into TACQUIS(FKUE,NUM,ACQUIS,POURCENTAGE) values ('IPAP',3,'de mettre en oeuvre des procédures de test',20);
merge into TACQUIS(FKUE,NUM,ACQUIS,POURCENTAGE) values ('IPAP',4,'de justifier la démarche mise en oeuvre dans l''élaboration du (ou des) programme(s)',20);
--association UE Section
merge into TSEC_UE (FKSECTION,FKUE) values ('INFO','IPAP');

--IPID
merge into TUE(CODE,ECTS,NB_PERIODES,NOM,REF,PRGM) values('IPID',9,100,'PROJET D''INTEGRATION DE DEVELOPPEMENT','7534 35 U32 D2',
cast('* de décrire, de caractériser et de produire le cahier des charges du projet;
* d''identifier les acteurs (collaborateurs, prestataires de service, etc.) intervenant dans la
		réalisation d''un projet d''intégration d''une application, de caractériser leurs rôles, leurs
		droits et leurs responsabilités ;
* de construire un dossier technique reprenant les différentes étapes;
* de mettre en oeuvre le projet en développant, parmi les concepts suivants:
	o la gestion des contenus dynamiques au travers d''une interface administrateur sécurisé,
	o la pagination de l''affichage des résultats d''une requête,
	o l''intégration de services internes et tiers,
	o la gestion de sélections, de filtres et de recherches au sein de l''application,
	o la gestion de la sécurisation et des droits d''accès aux contenus (administrateur,
			utilisateur public, utilisateur enregistré, gestionnaire, etc.),
	o l''affichage différencié des contenus (accessibilité, langue, sécurité,
					  fonctionnalités, disponibilité de l''information, etc.), en fonction des profils utilisateurs,
	o la programmation asynchrone (AJAX…),
	o l''optimisation du code, du cache et des échanges avec la base de données,
	o l''interaction avec un système de gestion de bases de données (récupérer, ajouter, modifier, supprimer des enregistrements, etc.) ;;
	o la programmation orientée objet,
	o l''exploitation d''un framework backend et d''un framework frontend (par exemple React Native),
	o etc.;
* d''identifier des menaces et de sécuriser le site en exploitant par exemple :
	o l''utilisation des outils spécifiques de protection et d''identification,
	o la protection contre des injections SQL, des attaques XSS, des vols de session, par détournement de cookies, etc.,
	o la réécriture d''url,
	o les paramétrages et les restrictions d''accès au serveur,
	o etc. ;
* de gérer des erreurs de programmation au moyen d''outils ou de techniques de débogage et d''y apporter une solution pertinente ;
* d''utiliser à bon escient la documentation disponible.
'as CLOB));

merge into TACQUIS(FKUE,NUM,ACQUIS,POURCENTAGE) values ('IPID',1,'de produire et défendre un cahier des charges et son dossier technique par rapport à la proposition du chargé de cours',50);
merge into TACQUIS(FKUE,NUM,ACQUIS,POURCENTAGE) values ('IPID',2,'d''implémenter une base de données et l''intégrité des données',30);
merge into TACQUIS(FKUE,NUM,ACQUIS,POURCENTAGE) values ('IPID',3,'de déployer et de justifier le site répondant aux consignes figurant dans le cahier des charges',20);
--association UE Section
merge into TSEC_UE (FKSECTION,FKUE) values ('INFO','IPID');





MERGE into TUSER(username,email,nom,prenom) values
('dvo','vo@isfce.be','VO','Didier'),
('et1','et1@isfce.be','Nom Et1','Prénom Et1'),
('et2','et2@isfce.be','Nom Et2','Prénom Et2');

INSERT INTO TDOSSIER(ID,DATE_CREATION,ETAT,OBJET_DEMANDE,FK_USER,COMPLET) VALUES (1,'2025-01-10','CLOTURE_ACCORDE','Demande de dispense','et1',true);
INSERT INTO TDOSSIER(ID,DATE_CREATION,ETAT,OBJET_DEMANDE,FK_USER,COMPLET) VALUES (2,'2026-01-19','CLOTURE_REFUSE','Demande de dispense','et1',true);
INSERT INTO TDOSSIER(ID,DATE_CREATION,ETAT,OBJET_DEMANDE,FK_USER,COMPLET) VALUES (3,'2026-01-22','DEMANDE_EN_COURS','Demande de dispense pour 3 cours','et1',false);
INSERT INTO TDOSSIER(ID,DATE_CREATION,ETAT,OBJET_DEMANDE,FK_USER,COMPLET) VALUES (4,'2026-01-18','DEMANDE_EN_COURS','Demande de dispense en venant de l''ULB','et2',false);

-- === ECOLES (base de connaissances — 5 établissements du PDF §1.3.2) ===
INSERT INTO TECOLE(CODE, NOM, URL_SITE) VALUES ('LDV', 'École Léonard de Vinci', 'https://www.vinci.be');
INSERT INTO TECOLE(CODE, NOM, URL_SITE) VALUES ('ULB', 'ULB Informatique', 'https://www.ulb.be');
INSERT INTO TECOLE(CODE, NOM, URL_SITE) VALUES ('HELB', 'HELB', 'https://www.helb-prigogine.be');
INSERT INTO TECOLE(CODE, NOM, URL_SITE) VALUES ('HE2B', 'HE2B', 'https://www.he2b.be');
INSERT INTO TECOLE(CODE, NOM, URL_SITE) VALUES ('EPHEC', 'EPHEC', 'https://www.ephec.be');

-- === CORRESPONDANCES ===
INSERT INTO TCORRESPONDANCE(ID, FK_ECOLE, DATE_VALIDATION, NOTES, ECTS_MIN_EXTERNE) VALUES (1, 'LDV', '2025-02-26', 'Bonne correspondance', NULL);
INSERT INTO TCORRESPONDANCE(ID, FK_ECOLE, DATE_VALIDATION, NOTES, ECTS_MIN_EXTERNE) VALUES (2, 'ULB', '2025-02-26', 'Programme Bloc 1', NULL);

-- === COURS EXTERNES ===
INSERT INTO TCORR_COURS(ID, FK_CORRESPONDANCE, CODE_COURS, INTITULE, ECTS, URL_FICHE_OFFIC) VALUES (1, 1, 'BINV1010-1', 'Programmation Java avancée', 6, NULL);
INSERT INTO TCORR_COURS(ID, FK_CORRESPONDANCE, CODE_COURS, INTITULE, ECTS, URL_FICHE_OFFIC) VALUES (2, 1, 'BINV2090-2', 'Projet de développement', 8, NULL);
INSERT INTO TCORR_COURS(ID, FK_CORRESPONDANCE, CODE_COURS, INTITULE, ECTS, URL_FICHE_OFFIC) VALUES (3, 2, 'INFO-F101', 'Informatique 1', 10, NULL);

-- === MAPPING CORRESPONDANCES → UEs ISFCE ===
INSERT INTO TCORR_UE(FK_CORRESPONDANCE, FK_UE) VALUES (1, 'IPAP');
INSERT INTO TCORR_UE(FK_CORRESPONDANCE, FK_UE) VALUES (1, 'IPID');
INSERT INTO TCORR_UE(FK_CORRESPONDANCE, FK_UE) VALUES (2, 'IPAP');

-- === COURS ETUDIANTS (liés au dossier 3 = et1 en cours) ===
INSERT INTO TCOURS_ETUDIANT(ID, FK_DOSSIER, FK_ECOLE, ECOLE_SAISIE, CODE_COURS, INTITULE, ECTS, FK_CORR_COURS, URL_FICHE, STATUT_SAISIE) VALUES
(1, 3, 'LDV', NULL, 'BINV1010-1', 'Programmation Java avancée', 6, 1, NULL, 'AUTO_RECONNU');
INSERT INTO TCOURS_ETUDIANT(ID, FK_DOSSIER, FK_ECOLE, ECOLE_SAISIE, CODE_COURS, INTITULE, ECTS, FK_CORR_COURS, URL_FICHE, STATUT_SAISIE) VALUES
(2, 3, NULL, 'Autre école inconnue', 'PROG101', 'Introduction programmation', 5, NULL, NULL, 'INCONNU');
INSERT INTO TCOURS_ETUDIANT(ID, FK_DOSSIER, FK_ECOLE, ECOLE_SAISIE, CODE_COURS, INTITULE, ECTS, FK_CORR_COURS, URL_FICHE, STATUT_SAISIE) VALUES
(3, 4, 'ULB', NULL, 'INFO-F101', 'Informatique 1', 10, 3, NULL, 'AUTO_RECONNU');

-- === DOCUMENTS (liés au dossier 3) ===
INSERT INTO TDOCUMENT(ID, FK_DOSSIER, FK_COURS_ETUDIANT, FK_ECOLE_DOC, ECOLE_SAISIE_DOC, TYPE_DOC, ORIGINAL_FILENAME, CHEMIN_RELATIF, TYPE_MIME, TAILLE, DATE_DEPOT, DELETED_AT, HASH_SHA256) VALUES
(1, 3, NULL, 'LDV', NULL, 'BULLETIN', 'bulletin_ldv.pdf', '/docs/et1/bulletin_ldv.pdf', 'application/pdf', 102400, '2026-01-22 10:00:00', NULL, NULL);
INSERT INTO TDOCUMENT(ID, FK_DOSSIER, FK_COURS_ETUDIANT, FK_ECOLE_DOC, ECOLE_SAISIE_DOC, TYPE_DOC, ORIGINAL_FILENAME, CHEMIN_RELATIF, TYPE_MIME, TAILLE, DATE_DEPOT, DELETED_AT, HASH_SHA256) VALUES
(2, NULL, 1, 'LDV', NULL, 'PROGRAMME_COURS', 'programme_java.pdf', '/docs/et1/programme_java.pdf', 'application/pdf', 51200, '2026-01-22 10:05:00', NULL, NULL);

-- === DISPENSES (liées au dossier 1 = et1 clôturé accordé) ===
INSERT INTO TDISPENSE(ID, FK_DOSSIER, FK_UE, DECISION, NOTE, DATE_DECISION, FK_VALIDATEUR, COMMENTAIRE) VALUES
(1, 1, 'IPAP', 'ACCORDEE', 14.50, '2025-01-20', 'dvo', 'Bonne correspondance avec LDV');
INSERT INTO TDISPENSE(ID, FK_DOSSIER, FK_UE, DECISION, NOTE, DATE_DECISION, FK_VALIDATEUR, COMMENTAIRE) VALUES
(2, 1, 'IPID', 'ACCORDEE', 12.00, '2025-01-20', 'dvo', NULL);

-- === DISPENSES (liées au dossier 2 = et1 clôturé refusé) ===
INSERT INTO TDISPENSE(ID, FK_DOSSIER, FK_UE, DECISION, NOTE, DATE_DECISION, FK_VALIDATEUR, COMMENTAIRE) VALUES
(3, 2, 'IPAP', 'REFUSEE', NULL, '2026-01-25', 'dvo', 'Programme trop différent');

