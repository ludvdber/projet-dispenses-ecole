package org.isfce.pid.connaissances;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.isfce.pid.dao.ICorrCoursDao;
import org.isfce.pid.dao.ICorrespondanceDao;
import org.isfce.pid.dao.IEcoleDao;
import org.isfce.pid.model.Correspondance;
import org.isfce.pid.model.CorrCours;
import org.isfce.pid.model.Ecole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import jakarta.transaction.Transactional;

/**
 * Tests DAO pour la base de connaissances : écoles, correspondances, cours
 * externes.
 *
 * @author Ludovic
 */
@SuppressWarnings("null")
@ActiveProfiles(value = "testU")
@Sql({ "/dataTestU.sql" })
@SpringBootTest
class TestDaoEcoleCorrespondance {

	@Autowired
	IEcoleDao daoEcole;
	@Autowired
	ICorrespondanceDao daoCorrespondance;
	@Autowired
	ICorrCoursDao daoCorrCours;

	// ======================== ECOLE ========================

	@Test
	@Transactional
	void testFindEcole() {
		var oLdv = daoEcole.findById("LDV");
		assertTrue(oLdv.isPresent());
		assertEquals("École Léonard de Vinci", oLdv.get().getNom());
		assertEquals("https://www.vinci.be", oLdv.get().getUrlSite());
	}

	@Test
	@Transactional
	void testCreateAndDeleteEcole() {
		long countBefore = daoEcole.count(); // 5 écoles du PDF (LDV, ULB, HELB, HE2B, EPHEC)
		Ecole newEcole = new Ecole("TEST", "École Test", "https://www.test.be");
		daoEcole.save(newEcole);

		assertTrue(daoEcole.existsById("TEST"));
		assertEquals(countBefore + 1, daoEcole.count());

		daoEcole.deleteById("TEST");
		assertFalse(daoEcole.existsById("TEST"));
		assertEquals(countBefore, daoEcole.count());
	}

	@Test
	@Transactional
	void testUpdateEcole() {
		var ecole = daoEcole.findById("ULB").get();
		// Ecole uses @Getter only, no @Setter — but we can save a new object
		Ecole updated = new Ecole("ULB", "ULB Sciences Informatiques", ecole.getUrlSite());
		daoEcole.save(updated);

		assertEquals("ULB Sciences Informatiques", daoEcole.findById("ULB").get().getNom());
	}

	// ======================== CORRESPONDANCE ========================

	@Test
	@Transactional
	void testFindCorrespondanceByEcole() {
		var corrs = daoCorrespondance.findByEcoleCode("LDV");
		assertEquals(1, corrs.size());
		assertEquals("Bonne correspondance", corrs.get(0).getNotes());
	}

	@Test
	@Transactional
	void testCreateCorrespondance() {
		Ecole ulb = daoEcole.findById("ULB").get();
		Correspondance newCorr = new Correspondance();
		newCorr.setEcole(ulb);
		newCorr.setDateValidation(LocalDate.of(2025, 2, 26));
		newCorr.setNotes("Nouvelle correspondance test");
		newCorr.setEctsMinExterne(10);

		newCorr = daoCorrespondance.save(newCorr);
		assertNotNull(newCorr.getId());

		var found = daoCorrespondance.findById(newCorr.getId());
		assertTrue(found.isPresent());
		assertEquals(10, found.get().getEctsMinExterne());
	}

	// ======================== CORR COURS ========================

	@Test
	@Transactional
	void testFindCorrCoursByCorrespondance() {
		var coursLdv = daoCorrCours.findByCorrespondanceId(1L);
		assertEquals(2, coursLdv.size()); // BINV1010-1 et BINV2090-2
	}

	@Test
	@Transactional
	void testCreateCorrCours() {
		Correspondance corr = daoCorrespondance.findById(1L).get();
		CorrCours newCours = new CorrCours();
		newCours.setCorrespondance(corr);
		newCours.setCodeCours("BINV-NEW");
		newCours.setIntitule("Nouveau cours test");
		newCours.setEcts(5);

		newCours = daoCorrCours.save(newCours);
		assertNotNull(newCours.getId());

		var coursLdv = daoCorrCours.findByCorrespondanceId(1L);
		assertEquals(3, coursLdv.size());
	}

	@Test
	@Transactional
	void testDeleteCorrCours() {
		long countBefore = daoCorrCours.count();
		daoCorrCours.deleteById(2L); // corrCours 1 est référencé par cours_etudiant (FK)
		assertEquals(countBefore - 1, daoCorrCours.count());
	}

	// ======================== ECOLE NON EXISTANTE ========================

	@Test
	@Transactional
	void testFindEcoleNonExistante() {
		assertFalse(daoEcole.existsById("FAKE"));
		assertTrue(daoEcole.findById("FAKE").isEmpty());
	}

	@Test
	@Transactional
	void testCountAll() {
		assertEquals(5, daoEcole.count()); // LDV, ULB, HELB, HE2B, EPHEC
		assertEquals(2, daoCorrespondance.count()); // 1 LDV + 1 ULB (dataTestU)
		assertEquals(3, daoCorrCours.count()); // 2 LDV + 1 ULB (dataTestU)
	}
}
