package org.isfce.pid.dispense;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.isfce.pid.dao.IDossierDao;
import org.isfce.pid.dao.IDispenseDao;
import org.isfce.pid.dao.IUserDao;
import org.isfce.pid.dao.UeDao;
import org.isfce.pid.model.DecisionDispense;
import org.isfce.pid.model.Dispense;
import org.isfce.pid.model.Dossier;
import org.isfce.pid.model.UE;
import org.isfce.pid.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import jakarta.transaction.Transactional;

/**
 * Tests DAO pour les dispenses : création, validation, refus, requêtes.
 *
 * @author Ludovic
 */
@SuppressWarnings("null")
@ActiveProfiles(value = "testU")
@Sql({ "/dataTestU.sql" })
@SpringBootTest
class TestDaoDispense {

	@Autowired
	IDispenseDao daoDispense;
	@Autowired
	IDossierDao daoDossier;
	@Autowired
	IUserDao daoUser;
	@Autowired
	UeDao daoUe;

	// ======================== QUERIES SUR DONNEES EXISTANTES ========================

	@Test
	@Transactional
	void testFindDispensesByDossier() {
		// Dossier 1 (et1, CLOTURE_ACCORDE) : 2 dispenses (IPAP + IPID)
		var dispenses = daoDispense.findByDossierId(1L);
		assertEquals(2, dispenses.size());
	}

	@Test
	@Transactional
	void testFindDispenseByDossierEtUe() {
		var oDisp = daoDispense.findByDossierIdAndUeCode(1L, "IPAP");
		assertTrue(oDisp.isPresent());
		assertEquals(DecisionDispense.ACCORDEE, oDisp.get().getDecision());
		assertEquals(new BigDecimal("14.50"), oDisp.get().getNote());
		assertEquals("dvo", oDisp.get().getValidateur().getUsername());
	}

	@Test
	@Transactional
	void testFindDispenseRefusee() {
		// Dossier 2 (et1, CLOTURE_REFUSE) : 1 dispense IPAP refusée
		var oDisp = daoDispense.findByDossierIdAndUeCode(2L, "IPAP");
		assertTrue(oDisp.isPresent());
		assertEquals(DecisionDispense.REFUSEE, oDisp.get().getDecision());
		assertEquals("Programme trop différent", oDisp.get().getCommentaire());
	}

	@Test
	@Transactional
	void testCountDispensesByDossier() {
		assertEquals(2, daoDispense.countByDossierId(1L));
		assertEquals(1, daoDispense.countByDossierId(2L));
		assertEquals(0, daoDispense.countByDossierId(3L)); // dossier en cours, pas encore de dispenses
	}

	// ======================== CREATION DISPENSE ========================

	@Test
	@Transactional
	void testCreateDispenseSansDecision() {
		// Crée une dispense en attente pour le dossier 3 (et1, en cours)
		Dossier dossier = daoDossier.findById(3L).get();
		UE ipap = daoUe.findById("IPAP").get();

		Dispense disp = new Dispense();
		disp.setDossier(dossier);
		disp.setUe(ipap);
		// decision reste null = en attente

		disp = daoDispense.save(disp);
		assertNotNull(disp.getId());

		var found = daoDispense.findByDossierIdAndUeCode(3L, "IPAP");
		assertTrue(found.isPresent());
		assertTrue(found.get().getDecision() == null); // en attente
	}

	// ======================== VALIDATION D'UNE DISPENSE ========================

	@Test
	@Transactional
	void testValiderDispense() {
		// Crée et valide une dispense
		Dossier dossier = daoDossier.findById(3L).get();
		UE ipid = daoUe.findById("IPID").get();
		User validateur = daoUser.findById("dvo").get();

		Dispense disp = new Dispense();
		disp.setDossier(dossier);
		disp.setUe(ipid);
		disp = daoDispense.save(disp);

		// Le professeur/direction valide
		disp.setDecision(DecisionDispense.ACCORDEE);
		disp.setNote(new BigDecimal("15.00"));
		disp.setDateDecision(LocalDate.now());
		disp.setValidateur(validateur);
		disp.setCommentaire("Correspondance validée");
		daoDispense.save(disp);

		var found = daoDispense.findById(disp.getId()).get();
		assertEquals(DecisionDispense.ACCORDEE, found.getDecision());
		assertEquals(new BigDecimal("15.00"), found.getNote());
		assertEquals(LocalDate.now(), found.getDateDecision());
		assertEquals("dvo", found.getValidateur().getUsername());
	}

	// ======================== REFUS D'UNE DISPENSE ========================

	@Test
	@Transactional
	void testRefuserDispense() {
		Dossier dossier = daoDossier.findById(4L).get(); // et2, en cours
		UE ipap = daoUe.findById("IPAP").get();
		User validateur = daoUser.findById("dvo").get();

		Dispense disp = new Dispense();
		disp.setDossier(dossier);
		disp.setUe(ipap);
		disp = daoDispense.save(disp);

		disp.setDecision(DecisionDispense.REFUSEE);
		disp.setDateDecision(LocalDate.now());
		disp.setValidateur(validateur);
		disp.setCommentaire("ECTS insuffisants");
		daoDispense.save(disp);

		var found = daoDispense.findById(disp.getId()).get();
		assertEquals(DecisionDispense.REFUSEE, found.getDecision());
		assertTrue(found.getNote() == null); // pas de note si refusée
		assertEquals("ECTS insuffisants", found.getCommentaire());
	}

	// ======================== SUPPRESSION DISPENSE ========================

	@Test
	@Transactional
	void testSupprimerDispense() {
		long countBefore = daoDispense.count();
		daoDispense.deleteById(1L);
		assertEquals(countBefore - 1, daoDispense.count());
		assertFalse(daoDispense.findById(1L).isPresent());
	}

	@Test
	@Transactional
	void testSupprimerToutesDispensesDossier() {
		var dispenses = daoDispense.findByDossierId(1L);
		assertEquals(2, dispenses.size());

		daoDispense.deleteAll(dispenses);
		assertEquals(0, daoDispense.countByDossierId(1L));
	}

	// ======================== DISPENSE NON EXISTANTE ========================

	@Test
	@Transactional
	void testDispenseNonExistante() {
		assertFalse(daoDispense.findByDossierIdAndUeCode(999L, "IPAP").isPresent());
		assertFalse(daoDispense.findByDossierIdAndUeCode(1L, "FAKE").isPresent());
	}
}
