package org.isfce.pid.ecole;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.hamcrest.Matchers;
import org.isfce.pid.dao.ICorrCoursDao;
import org.isfce.pid.dao.IEcoleDao;
import org.isfce.pid.model.CorrCours;
import org.isfce.pid.model.Ecole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Tests du contrôleur REST EcoleControllerRest (base de connaissances).
 *
 * @author Ludovic
 */
@SuppressWarnings("null")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles(profiles = "testU")
public class TestEcoleController {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private IEcoleDao ecoleDaoMock;

	@MockitoBean
	private ICorrCoursDao corrCoursDaoMock;

	@BeforeEach
	void setUp() {
		// 5 écoles du CdC §1.3.2
		var ldv = new Ecole("LDV", "Léonard de Vinci", "https://www.ldv.be");
		var ulb = new Ecole("ULB", "ULB Informatique", "https://www.ulb.be");
		var helb = new Ecole("HELB", "HELB", "https://www.helb.be");
		var he2b = new Ecole("HE2B", "HE2B", "https://www.he2b.be");
		var ephec = new Ecole("EPHEC", "EPHEC", "https://www.ephec.be");

		when(ecoleDaoMock.findAll()).thenReturn(List.of(ldv, ulb, helb, he2b, ephec));

		// Cours LDV (1 exemple)
		var cc = new CorrCours();
		cc.setId(3L);
		cc.setCodeCours("BINV1010-1");
		cc.setIntitule("Principes algorithmiques et programmation");
		cc.setEcts(6);
		when(corrCoursDaoMock.findByCorrespondanceEcoleCode("LDV")).thenReturn(List.of(cc));

		// Ecole inconnue → liste vide
		when(corrCoursDaoMock.findByCorrespondanceEcoleCode("INCONNU")).thenReturn(List.of());
	}

	@Test
	@WithMockUser(username = "et1", roles = "ETUDIANT")
	void testGetEcoles() throws Exception {
		mockMvc.perform(get("/api/ecole"))
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/json"))
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$", Matchers.hasSize(5)))
				.andExpect(jsonPath("$[?(@.code == 'LDV')]").exists())
				.andExpect(jsonPath("$[?(@.code == 'ULB')]").exists())
				.andExpect(jsonPath("$[?(@.code == 'HELB')]").exists())
				.andExpect(jsonPath("$[?(@.code == 'HE2B')]").exists())
				.andExpect(jsonPath("$[?(@.code == 'EPHEC')]").exists());
	}

	@Test
	@WithMockUser(username = "et1", roles = "ETUDIANT")
	void testGetCoursEcole_LDV() throws Exception {
		mockMvc.perform(get("/api/ecole/LDV/cours"))
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/json"))
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$", Matchers.hasSize(1)))
				.andExpect(jsonPath("$[0].codeCours").value("BINV1010-1"))
				.andExpect(jsonPath("$[0].intitule").value("Principes algorithmiques et programmation"))
				.andExpect(jsonPath("$[0].ects").value(6));
	}

	@Test
	@WithMockUser(username = "et1", roles = "ETUDIANT")
	void testGetCoursEcole_Inconnu() throws Exception {
		mockMvc.perform(get("/api/ecole/INCONNU/cours"))
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/json"))
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$", Matchers.hasSize(0)));
	}
}
