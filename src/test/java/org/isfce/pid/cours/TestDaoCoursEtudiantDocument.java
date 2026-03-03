package org.isfce.pid.cours;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.isfce.pid.dao.ICorrCoursDao;
import org.isfce.pid.dao.ICoursEtudiantDao;
import org.isfce.pid.dao.IDocumentDao;
import org.isfce.pid.dao.IDossierDao;
import org.isfce.pid.dao.IEcoleDao;
import org.isfce.pid.model.CoursEtudiant;
import org.isfce.pid.model.Document;
import org.isfce.pid.model.Dossier;
import org.isfce.pid.model.Ecole;
import org.isfce.pid.model.StatutSaisie;
import org.isfce.pid.model.TypeDoc;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import jakarta.transaction.Transactional;

/**
 * Tests DAO pour les cours étudiants et les documents : CRUD, filtres,
 * soft-delete.
 *
 * @author Ludovic
 */
@SuppressWarnings("null")
@ActiveProfiles(value = "testU")
@Sql({ "/dataTestU.sql" })
@SpringBootTest
class TestDaoCoursEtudiantDocument {

	@Autowired
	ICoursEtudiantDao daoCoursEtu;
	@Autowired
	IDocumentDao daoDocument;
	@Autowired
	IDossierDao daoDossier;
	@Autowired
	IEcoleDao daoEcole;
	@Autowired
	ICorrCoursDao daoCorrCours;

	// ======================== COURS ETUDIANT — QUERIES ========================

	@Test
	@Transactional
	void testFindCoursEtudiantByDossier() {
		// Dossier 3 (et1, en cours) : 2 cours
		var cours = daoCoursEtu.findByDossierId(3L);
		assertEquals(2, cours.size());
	}

	@Test
	@Transactional
	void testCountCoursEtudiantByDossier() {
		assertEquals(2, daoCoursEtu.countByDossierId(3L));
		assertEquals(1, daoCoursEtu.countByDossierId(4L));
		assertEquals(0, daoCoursEtu.countByDossierId(1L)); // dossier clôturé sans cours
	}

	@Test
	@Transactional
	void testCoursEtudiantAvecEcoleConnue() {
		// cours 1 = LDV, école connue
		var cours = daoCoursEtu.findById(1L).get();
		assertNotNull(cours.getEcole());
		assertEquals("LDV", cours.getEcole().getCode());
		assertTrue(cours.getEcoleSaisie() == null || cours.getEcoleSaisie().isBlank());
		assertEquals(StatutSaisie.AUTO_RECONNU, cours.getStatutSaisie());
		assertNotNull(cours.getCorrCours()); // lié à un cours de correspondance
	}

	@Test
	@Transactional
	void testCoursEtudiantAvecEcoleSaisie() {
		// cours 2 = école saisie librement
		var cours = daoCoursEtu.findById(2L).get();
		assertTrue(cours.getEcole() == null);
		assertEquals("Autre école inconnue", cours.getEcoleSaisie());
		assertEquals(StatutSaisie.INCONNU, cours.getStatutSaisie());
		assertTrue(cours.getCorrCours() == null); // pas de correspondance connue
	}

	// ======================== COURS ETUDIANT — CREATION ========================

	@Test
	@Transactional
	void testCreateCoursAvecEcoleConnue() {
		Dossier dossier = daoDossier.findById(4L).get(); // et2
		Ecole ulb = daoEcole.findById("ULB").get();

		CoursEtudiant cours = new CoursEtudiant();
		cours.setDossier(dossier);
		cours.setEcole(ulb);
		cours.setCodeCours("INFO-F102");
		cours.setIntitule("Informatique 2");
		cours.setEcts(5);
		cours.setStatutSaisie(StatutSaisie.A_COMPLETER);

		cours = daoCoursEtu.save(cours);
		assertNotNull(cours.getId());
		assertEquals(2, daoCoursEtu.countByDossierId(4L));
	}

	@Test
	@Transactional
	void testCreateCoursAvecEcoleSaisie() {
		Dossier dossier = daoDossier.findById(4L).get();

		CoursEtudiant cours = new CoursEtudiant();
		cours.setDossier(dossier);
		cours.setEcoleSaisie("Mon ancienne école");
		cours.setCodeCours("MATH101");
		cours.setIntitule("Mathématiques 1");
		cours.setEcts(4);
		cours.setStatutSaisie(StatutSaisie.INCONNU);

		cours = daoCoursEtu.save(cours);
		assertNotNull(cours.getId());
	}

	// ======================== COURS ETUDIANT — MODIFICATION ========================

	@Test
	@Transactional
	void testModifierCoursEtudiant() {
		var cours = daoCoursEtu.findById(2L).get(); // cours inconnu
		cours.setStatutSaisie(StatutSaisie.A_COMPLETER);
		cours.setEcts(8);
		daoCoursEtu.save(cours);

		var reloaded = daoCoursEtu.findById(2L).get();
		assertEquals(StatutSaisie.A_COMPLETER, reloaded.getStatutSaisie());
		assertEquals(8, reloaded.getEcts());
	}

	// ======================== COURS ETUDIANT — SUPPRESSION ========================

	@Test
	@Transactional
	void testSupprimerCoursEtudiant() {
		long countBefore = daoCoursEtu.count();
		daoCoursEtu.deleteById(2L);
		assertEquals(countBefore - 1, daoCoursEtu.count());
		assertEquals(1, daoCoursEtu.countByDossierId(3L));
	}

	// ======================== DOCUMENTS — QUERIES ========================

	@Test
	@Transactional
	void testFindDocumentsByDossier() {
		// Dossier 3 : 1 document lié au dossier (bulletin)
		var docs = daoDocument.findByDossierIdAndDeletedAtIsNull(3L);
		assertEquals(1, docs.size());
		assertEquals(TypeDoc.BULLETIN, docs.get(0).getTypeDoc());
		assertEquals("bulletin_ldv.pdf", docs.get(0).getOriginalFilename());
	}

	@Test
	@Transactional
	void testFindDocumentsByCoursEtudiant() {
		// Cours 1 : 1 document (programme)
		var docs = daoDocument.findByCoursEtudiantIdAndDeletedAtIsNull(1L);
		assertEquals(1, docs.size());
		assertEquals(TypeDoc.PROGRAMME_COURS, docs.get(0).getTypeDoc());
	}

	@Test
	@Transactional
	void testCountDocumentsByDossier() {
		assertEquals(1, daoDocument.countByDossierIdAndDeletedAtIsNull(3L));
		assertEquals(0, daoDocument.countByDossierIdAndDeletedAtIsNull(4L));
	}

	// ======================== DOCUMENTS — CREATION ========================

	@Test
	@Transactional
	void testCreateDocumentLieAuDossier() {
		Dossier dossier = daoDossier.findById(4L).get();

		Document doc = new Document();
		doc.setDossier(dossier);
		// coursEtudiant reste null (XOR)
		doc.setTypeDoc(TypeDoc.BULLETIN);
		doc.setOriginalFilename("bulletin_ulb.pdf");
		doc.setCheminRelatif("/docs/et2/bulletin_ulb.pdf");
		doc.setTypeMime("application/pdf");
		doc.setTaille(200000L);
		doc.setDateDepot(LocalDateTime.now());

		doc = daoDocument.save(doc);
		assertNotNull(doc.getId());
		assertEquals(1, daoDocument.countByDossierIdAndDeletedAtIsNull(4L));
	}

	@Test
	@Transactional
	void testCreateDocumentLieAuCours() {
		CoursEtudiant cours = daoCoursEtu.findById(3L).get(); // cours ULB de et2

		Document doc = new Document();
		// dossier reste null (XOR)
		doc.setCoursEtudiant(cours);
		doc.setEcoleDoc(daoEcole.findById("ULB").get());
		doc.setTypeDoc(TypeDoc.PROGRAMME_COURS);
		doc.setOriginalFilename("fiche_info_f101.pdf");
		doc.setCheminRelatif("/docs/et2/fiche_info_f101.pdf");
		doc.setTypeMime("application/pdf");
		doc.setTaille(80000L);
		doc.setDateDepot(LocalDateTime.now());

		doc = daoDocument.save(doc);
		assertNotNull(doc.getId());
	}

	// ======================== DOCUMENTS — SOFT DELETE ========================

	@Test
	@Transactional
	void testSoftDeleteDocument() {
		var doc = daoDocument.findById(1L).get(); // bulletin de dossier 3
		assertTrue(doc.getDeletedAt() == null); // actif

		doc.setDeletedAt(LocalDateTime.now());
		daoDocument.save(doc);

		// findByDossierIdAndDeletedAtIsNull ne le retourne plus
		var docsActifs = daoDocument.findByDossierIdAndDeletedAtIsNull(3L);
		assertEquals(0, docsActifs.size());

		// Mais il existe toujours en BD
		assertTrue(daoDocument.findById(1L).isPresent());
		assertNotNull(daoDocument.findById(1L).get().getDeletedAt());
	}

	// ======================== DOCUMENTS — COUNT EXCLUT SOFT-DELETED ========================

	@Test
	@Transactional
	void testCountDocumentsExclutSoftDeleted() {
		// Dossier 3 : 1 document actif (bulletin)
		assertEquals(1, daoDocument.countByDossierIdAndDeletedAtIsNull(3L));

		// Soft-delete ce document
		var doc = daoDocument.findByDossierIdAndDeletedAtIsNull(3L).get(0);
		doc.setDeletedAt(LocalDateTime.now());
		daoDocument.save(doc);

		// Le count ne doit plus le compter
		assertEquals(0, daoDocument.countByDossierIdAndDeletedAtIsNull(3L));

		// Mais le document existe toujours en BD
		assertTrue(daoDocument.findById(doc.getId()).isPresent());
	}

	// ======================== DOCUMENTS — SUPPRESSION PHYSIQUE ========================

	@Test
	@Transactional
	void testSuppressionPhysiqueDocument() {
		long countBefore = daoDocument.count();
		daoDocument.deleteById(1L);
		assertEquals(countBefore - 1, daoDocument.count());
		assertFalse(daoDocument.findById(1L).isPresent());
	}
}
