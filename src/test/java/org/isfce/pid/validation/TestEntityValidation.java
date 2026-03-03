package org.isfce.pid.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.isfce.pid.model.CoursEtudiant;
import org.isfce.pid.model.Document;
import org.isfce.pid.model.Dossier;
import org.isfce.pid.model.Ecole;
import org.isfce.pid.model.EtatDossier;
import org.isfce.pid.model.TypeDoc;
import org.isfce.pid.model.User;
import org.junit.jupiter.api.Test;

/**
 * Tests unitaires purs (sans Spring) pour les validations XOR
 * sur les entités CoursEtudiant et Document.
 *
 * @author Ludovic
 */
class TestEntityValidation {

	// ======================== CoursEtudiant — XOR école ========================

	@Test
	void testCoursEtudiant_EcoleConnue_Valide() {
		CoursEtudiant cours = new CoursEtudiant();
		cours.setEcole(new Ecole("LDV", "École LDV", null));
		cours.setEcoleSaisie(null);
		// ecole XOR ecoleSaisie → true XOR false = true ✓
		assertTrue(cours.isEcoleXorValid());
	}

	@Test
	void testCoursEtudiant_EcoleSaisie_Valide() {
		CoursEtudiant cours = new CoursEtudiant();
		cours.setEcole(null);
		cours.setEcoleSaisie("Mon ancienne école");
		// false XOR true = true ✓
		assertTrue(cours.isEcoleXorValid());
	}

	@Test
	void testCoursEtudiant_AucuneEcole_Invalide() {
		CoursEtudiant cours = new CoursEtudiant();
		cours.setEcole(null);
		cours.setEcoleSaisie(null);
		// false XOR false = false ✗
		assertFalse(cours.isEcoleXorValid());
	}

	@Test
	void testCoursEtudiant_LesDeuxEcoles_Invalide() {
		CoursEtudiant cours = new CoursEtudiant();
		cours.setEcole(new Ecole("LDV", "École LDV", null));
		cours.setEcoleSaisie("Aussi saisie");
		// true XOR true = false ✗
		assertFalse(cours.isEcoleXorValid());
	}

	@Test
	void testCoursEtudiant_EcoleSaisieVide_Invalide() {
		CoursEtudiant cours = new CoursEtudiant();
		cours.setEcole(null);
		cours.setEcoleSaisie("   "); // blank
		// false XOR false(isBlank) = false ✗
		assertFalse(cours.isEcoleXorValid());
	}

	// ======================== Document — XOR dossier/coursEtudiant ========================

	@Test
	void testDocument_LieAuDossier_Valide() {
		Document doc = createBaseDocument();
		doc.setDossier(createDossier());
		doc.setCoursEtudiant(null);
		// dossier!=null XOR coursEtudiant==null → false XOR true → true ✓
		// Wait — (dossier==null) ^ (coursEtudiant==null) → false ^ true = true ✓
		assertTrue(doc.isDossierXorCoursEtudiantValid());
	}

	@Test
	void testDocument_LieAuCours_Valide() {
		Document doc = createBaseDocument();
		doc.setDossier(null);
		doc.setCoursEtudiant(new CoursEtudiant());
		// (null==null) ^ (cours==null) → true ^ false = true ✓
		assertTrue(doc.isDossierXorCoursEtudiantValid());
	}

	@Test
	void testDocument_LieAuxDeux_Invalide() {
		Document doc = createBaseDocument();
		doc.setDossier(createDossier());
		doc.setCoursEtudiant(new CoursEtudiant());
		// false ^ false = false ✗
		assertFalse(doc.isDossierXorCoursEtudiantValid());
	}

	@Test
	void testDocument_LieAAucun_Invalide() {
		Document doc = createBaseDocument();
		doc.setDossier(null);
		doc.setCoursEtudiant(null);
		// true ^ true = false ✗
		assertFalse(doc.isDossierXorCoursEtudiantValid());
	}

	// ======================== Helpers ========================

	private Dossier createDossier() {
		User user = new User("et1", "et1@isfce.be", "Nom", "Prénom");
		return Dossier.builder()
				.user(user)
				.dateCreation(java.time.LocalDate.now())
				.etat(EtatDossier.DEMANDE_EN_COURS)
				.build();
	}

	private Document createBaseDocument() {
		Document doc = new Document();
		doc.setTypeDoc(TypeDoc.BULLETIN);
		doc.setOriginalFilename("test.pdf");
		doc.setCheminRelatif("/docs/test.pdf");
		doc.setTypeMime("application/pdf");
		doc.setTaille(1024L);
		doc.setDateDepot(LocalDateTime.now());
		return doc;
	}
}
