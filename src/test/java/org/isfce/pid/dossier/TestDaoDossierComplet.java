package org.isfce.pid.dossier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.isfce.pid.dao.ICoursEtudiantDao;
import org.isfce.pid.dao.IDispenseDao;
import org.isfce.pid.dao.IDocumentDao;
import org.isfce.pid.dao.IDossierDao;
import org.isfce.pid.dao.IUserDao;
import org.isfce.pid.model.Dossier;
import org.isfce.pid.model.EtatDossier;
import org.isfce.pid.model.User;
import org.isfce.pid.service.DossierService;
import org.isfce.pid.controller.error.DossierException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import jakarta.transaction.Transactional;

/**
 * Tests complets du cycle de vie d'un dossier : création, édition, soumission,
 * changements d'état, validation, refus, suppression.
 *
 * @author Ludovic
 */
@SuppressWarnings("null")
@ActiveProfiles(value = "testU")
@Sql({ "/dataTestU.sql" })
@SpringBootTest
class TestDaoDossierComplet {

	@Autowired
	IDossierDao daoDossier;
	@Autowired
	IUserDao daoUser;
	@Autowired
	DossierService dossierService;
	@Autowired
	ICoursEtudiantDao daoCoursEtu;
	@Autowired
	IDocumentDao daoDocument;
	@Autowired
	IDispenseDao daoDispense;

	// ======================== CREATION ========================

	@Test
	@Transactional
	void testCreateDossierViaService() throws DossierException {
		// et2 a déjà un dossier en cours → on utilise un nouvel utilisateur
		User newUser = daoUser.save(new User("et3", "et3@isfce.be", "Nom Et3", "Prénom Et3"));

		Dossier dossier = dossierService.createDossier(newUser, "Ma demande de dispense");

		assertNotNull(dossier.getId());
		assertEquals(EtatDossier.DEMANDE_EN_COURS, dossier.getEtat());
		assertEquals(LocalDate.now(), dossier.getDateCreation());
		assertEquals("Ma demande de dispense", dossier.getObjetDemande());
		assertFalse(dossier.isComplet());
		assertEquals(newUser.getUsername(), dossier.getUser().getUsername());
	}

	@Test
	@Transactional
	void testCreateDossierDoublonInterdit() {
		// et2 a déjà un dossier DEMANDE_EN_COURS → exception
		User et2 = daoUser.findById("et2").get();

		assertThrows(DossierException.class, () -> {
			dossierService.createDossier(et2, "Nouvelle demande");
		});
	}

	@Test
	@Transactional
	void testCreateDossierApresClotureAutorise() throws DossierException {
		// et1 a 2 dossiers clôturés + 1 en cours
		// On supprime d'abord le dossier en cours de et1 (+ ses dépendances FK)
		User et1 = daoUser.findById("et1").get();
		var dossierEnCours = daoDossier.findDossierEnCours(et1);
		assertTrue(dossierEnCours.isPresent());
		Long dossierId = dossierEnCours.get().getId();
		daoDocument.deleteAll(daoDocument.findByDossierIdAndDeletedAtIsNull(dossierId));
		// Documents liés aux cours de ce dossier
		daoCoursEtu.findByDossierId(dossierId).forEach(c ->
				daoDocument.deleteAll(daoDocument.findByCoursEtudiantIdAndDeletedAtIsNull(c.getId())));
		daoCoursEtu.deleteAll(daoCoursEtu.findByDossierId(dossierId));
		daoDispense.deleteAll(daoDispense.findByDossierId(dossierId));
		daoDossier.delete(dossierEnCours.get());

		// Maintenant et1 n'a plus de dossier en cours → peut en créer un nouveau
		Dossier nouveau = dossierService.createDossier(et1, "Nouvelle demande après clôture");
		assertNotNull(nouveau.getId());
		assertEquals(EtatDossier.DEMANDE_EN_COURS, nouveau.getEtat());
	}

	// ======================== EDITION (demande non complète) ========================

	@Test
	@Transactional
	void testEditerObjetDemande() {
		User et1 = daoUser.findById("et1").get();
		Dossier enCours = daoDossier.findDossierEnCours(et1).get();

		enCours.setObjetDemande("Objet modifié par l'étudiant");
		daoDossier.save(enCours);

		Dossier reloaded = daoDossier.findById(enCours.getId()).get();
		assertEquals("Objet modifié par l'étudiant", reloaded.getObjetDemande());
	}

	@Test
	@Transactional
	void testMarquerDossierComplet() {
		User et1 = daoUser.findById("et1").get();
		Dossier enCours = daoDossier.findDossierEnCours(et1).get();
		assertFalse(enCours.isComplet());

		enCours.setComplet(true);
		enCours.setDateSoumis(LocalDate.now());
		daoDossier.save(enCours);

		Dossier reloaded = daoDossier.findById(enCours.getId()).get();
		assertTrue(reloaded.isComplet());
		assertEquals(LocalDate.now(), reloaded.getDateSoumis());
	}

	// ======================== CHANGEMENTS D'ETAT (workflow) ========================

	@Test
	@Transactional
	void testTransitionVersTraitementDirection() {
		User et1 = daoUser.findById("et1").get();
		Dossier enCours = daoDossier.findDossierEnCours(et1).get();
		assertEquals(EtatDossier.DEMANDE_EN_COURS, enCours.getEtat());

		enCours.setEtat(EtatDossier.TRAITEMENT_DIRECTION);
		daoDossier.save(enCours);

		assertEquals(EtatDossier.TRAITEMENT_DIRECTION, daoDossier.findById(enCours.getId()).get().getEtat());
	}

	@Test
	@Transactional
	void testTransitionVersTraitementEnseignant() {
		User et1 = daoUser.findById("et1").get();
		Dossier enCours = daoDossier.findDossierEnCours(et1).get();

		enCours.setEtat(EtatDossier.TRAITEMENT_ENSEIGNANT);
		daoDossier.save(enCours);

		assertEquals(EtatDossier.TRAITEMENT_ENSEIGNANT, daoDossier.findById(enCours.getId()).get().getEtat());
	}

	@Test
	@Transactional
	void testTransitionAttenteComplement() {
		User et2 = daoUser.findById("et2").get();
		Dossier enCours = daoDossier.findDossierEnCours(et2).get();

		enCours.setEtat(EtatDossier.ATTENTE_COMPLEMENT);
		daoDossier.save(enCours);

		Dossier reloaded = daoDossier.findById(enCours.getId()).get();
		assertEquals(EtatDossier.ATTENTE_COMPLEMENT, reloaded.getEtat());
		// L'étudiant peut encore modifier le dossier
		assertFalse(reloaded.isComplet());
	}

	// ======================== VALIDATION (clôture accordée) ========================

	@Test
	@Transactional
	void testClotureAccordee() {
		User et2 = daoUser.findById("et2").get();
		Dossier enCours = daoDossier.findDossierEnCours(et2).get();

		enCours.setEtat(EtatDossier.CLOTURE_ACCORDE);
		enCours.setComplet(true);
		daoDossier.save(enCours);

		// Vérifie que le dossier n'apparaît plus comme "en cours"
		assertFalse(daoDossier.findDossierEnCours(et2).isPresent());
		assertEquals(1, daoDossier.getNbDossierCloture("et2"));
		assertEquals(0, daoDossier.getNbDossierEnCours("et2"));
	}

	// ======================== REFUS (clôture refusée) ========================

	@Test
	@Transactional
	void testClotureRefusee() {
		User et2 = daoUser.findById("et2").get();
		Dossier enCours = daoDossier.findDossierEnCours(et2).get();

		enCours.setEtat(EtatDossier.CLOTURE_REFUSE);
		enCours.setComplet(true);
		daoDossier.save(enCours);

		assertFalse(daoDossier.findDossierEnCours(et2).isPresent());
		assertEquals(1, daoDossier.getNbDossierCloture("et2"));
	}

	// ======================== SUPPRESSION TOTALE ========================

	@Test
	@Transactional
	void testSuppressionTotaleDossier() {
		User et2 = daoUser.findById("et2").get();
		int countBefore = daoDossier.countByUser(et2);
		Dossier enCours = daoDossier.findDossierEnCours(et2).get();
		Long id = enCours.getId();

		// Supprime les dépendances FK avant le dossier
		daoCoursEtu.findByDossierId(id).forEach(c ->
				daoDocument.deleteAll(daoDocument.findByCoursEtudiantIdAndDeletedAtIsNull(c.getId())));
		daoDocument.deleteAll(daoDocument.findByDossierIdAndDeletedAtIsNull(id));
		daoCoursEtu.deleteAll(daoCoursEtu.findByDossierId(id));
		daoDispense.deleteAll(daoDispense.findByDossierId(id));
		daoDossier.delete(enCours);

		assertFalse(daoDossier.findById(id).isPresent());
		assertEquals(countBefore - 1, daoDossier.countByUser(et2));
		assertEquals(0, daoDossier.getNbDossierEnCours("et2"));
	}

	@Test
	@Transactional
	void testSuppressionTousDossiers() {
		User et1 = daoUser.findById("et1").get();
		int count = daoDossier.countByUser(et1);
		assertTrue(count > 0);

		var dossiers = daoDossier.findDossierByUserOrderByDateCreationDesc(et1);
		// Supprime toutes les dépendances FK avant les dossiers
		for (Dossier d : dossiers) {
			Long id = d.getId();
			daoCoursEtu.findByDossierId(id).forEach(c ->
					daoDocument.deleteAll(daoDocument.findByCoursEtudiantIdAndDeletedAtIsNull(c.getId())));
			daoDocument.deleteAll(daoDocument.findByDossierIdAndDeletedAtIsNull(id));
			daoCoursEtu.deleteAll(daoCoursEtu.findByDossierId(id));
			daoDispense.deleteAll(daoDispense.findByDossierId(id));
		}
		daoDossier.deleteAll(dossiers);

		assertEquals(0, daoDossier.countByUser(et1));
		assertEquals(0, daoDossier.getNbDossierEnCours("et1"));
		assertEquals(0, daoDossier.getNbDossierCloture("et1"));
	}

	// ======================== QUERIES ========================

	@Test
	@Transactional
	void testFindDossierByUserOrderByDate() {
		User et1 = daoUser.findById("et1").get();
		var dossiers = daoDossier.findDossierByUserOrderByDateCreationDesc(et1);
		assertEquals(3, dossiers.size());
		// Vérifie l'ordre décroissant par date
		assertTrue(dossiers.get(0).getDateCreation().isAfter(dossiers.get(1).getDateCreation())
				|| dossiers.get(0).getDateCreation().isEqual(dossiers.get(1).getDateCreation()));
	}

	@Test
	@Transactional
	void testNbDossiers() {
		// et1 : 2 clôturés + 1 en cours
		assertEquals(1, daoDossier.getNbDossierEnCours("et1"));
		assertEquals(2, daoDossier.getNbDossierCloture("et1"));

		// et2 : 1 en cours
		assertEquals(1, daoDossier.getNbDossierEnCours("et2"));
		assertEquals(0, daoDossier.getNbDossierCloture("et2"));

		// dvo : aucun dossier
		assertEquals(0, daoDossier.getNbDossierEnCours("dvo"));
		assertEquals(0, daoDossier.getNbDossierCloture("dvo"));
	}

}
