package org.isfce.pid.dossier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.isfce.pid.dao.IDossierDao;
import org.isfce.pid.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import jakarta.transaction.Transactional;

@ActiveProfiles(value = "testU")
@Sql({ "/dataTestU.sql" })
@SpringBootTest
class TestDaoDossier {

	@Autowired
	IDossierDao daoDossier;

	@Test
	@Transactional
	void getNbDossier() {
		assertEquals(1, daoDossier.getNbDossierEnCours("et1"));
		assertEquals(2, daoDossier.getNbDossierCloture("et1"));
		assertEquals(1, daoDossier.getNbDossierEnCours("et2"));
		assertEquals(0, daoDossier.getNbDossierCloture("et2"));
	}
	@Test
	@Transactional
	void getDossierEnCours() {
		User user=new User("et1","et1@isfce.be","Nom Et1","Prénom Et1");
		assertTrue(daoDossier.findDossierEnCours(user).isPresent());
		
	}

}
