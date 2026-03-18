package org.isfce.pid.dossier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.isfce.pid.exception.DossierException;
import org.isfce.pid.dao.IUserDao;
import org.isfce.pid.model.Dossier;
import org.isfce.pid.model.EtatDossier;
import org.isfce.pid.model.User;
import org.isfce.pid.service.DossierService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import jakarta.transaction.Transactional;

/**
 * Tests de la couche service pour les dossiers.
 *
 * @author Ludovic
 */
@ActiveProfiles(value = "testU")
@Sql({ "/dataTestU.sql" })
@SpringBootTest
class TestDossierServiceComplet {

	@Autowired
	DossierService dossierService;
	@Autowired
	IUserDao daoUser;

	// ======================== createDossier ========================

	@Test
	@Transactional
	void testCreateDossierNouvelUtilisateur() throws DossierException {
		User newUser = daoUser.save(new User("et3", "et3@test.be", "Test", "User3"));

		Dossier dossier = dossierService.createDossier(newUser, "Demande test");

		assertNotNull(dossier.getId());
		assertEquals(EtatDossier.DEMANDE_EN_COURS, dossier.getEtat());
		assertEquals("Demande test", dossier.getObjetDemande());
		assertFalse(dossier.isComplet());
	}

	@Test
	@Transactional
	void testCreateDossierDoublon() {
		User et2 = daoUser.findById("et2").get();
		// et2 a déjà un dossier en cours → DossierException
		DossierException ex = assertThrows(DossierException.class, () -> {
			dossierService.createDossier(et2, "Doublon");
		});
		assertEquals("err.dossier.enCours", ex.getMessage());
	}

	@Test
	@Transactional
	void testCreateDossierObjetDemandeNull() throws DossierException {
		// objetDemande est nullable en base → le service ne bloque pas
		User newUser = daoUser.save(new User("et4", "et4@test.be", "Test", null));

		Dossier dossier = dossierService.createDossier(newUser, null);
		assertNotNull(dossier.getId());
		assertTrue(dossier.getObjetDemande() == null);
	}

	// ======================== getDossierEnCours ========================

	@Test
	@Transactional
	void testGetDossierEnCours() {
		User et1 = daoUser.findById("et1").get();
		var dossier = dossierService.getDossierEnCours(et1);
		assertTrue(dossier.isPresent());
		assertEquals(EtatDossier.DEMANDE_EN_COURS, dossier.get().getEtat());
	}

	@Test
	@Transactional
	void testGetDossierEnCoursSansResultat() {
		User dvo = daoUser.findById("dvo").get();
		// dvo n'a aucun dossier
		assertFalse(dossierService.getDossierEnCours(dvo).isPresent());
	}

}
