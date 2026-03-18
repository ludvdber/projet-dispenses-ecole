package org.isfce.pid.dispense;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collection;
import java.util.List;

import org.isfce.pid.exception.DossierException;
import org.isfce.pid.dto.CoursEtudiantDto;
import org.isfce.pid.dto.DispenseDto;
import org.isfce.pid.model.StatutSaisie;
import org.isfce.pid.service.DispenseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Tests MockMvc pour DispenseController.
 *
 * @author Ludovic
 */
@SuppressWarnings("null")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("testU")
class TestDispenseController {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private DispenseService dispenseServiceMock;

	private JwtAuthenticationToken tokenEt1;

	// --- Données de test ---

	private static final CoursEtudiantDto COURS1 = new CoursEtudiantDto(
			10L, 3L, "LDV", null, "BINV1010-1", "Programmation Java avancée",
			6, null, 1L, StatutSaisie.AUTO_RECONNU);

	private static final DispenseDto DISPENSE_OK = new DispenseDto(
			1L, 3L, "UE1", "Algorithmique", null, List.of(), false);

	private static final DispenseDto DISPENSE_AVEC_COURS = new DispenseDto(
			1L, 3L, "UE1", "Algorithmique", null, List.of(COURS1), true);

	@BeforeEach
	void setUp() throws DossierException {
		Jwt jwt = Jwt.withTokenValue("token").header("alg", "none")
				.claim("sub", "et1").claim("preferred_username", "et1")
				.claim("given_name", "Prénom Et1").claim("family_name", "Nom Et1")
				.claim("email", "et1@isfce.be").build();
		Collection<GrantedAuthority> auth = AuthorityUtils.createAuthorityList("ROLE_ETUDIANT");
		tokenEt1 = new JwtAuthenticationToken(jwt, auth);

		// Mock createDispense OK
		when(dispenseServiceMock.createDispense(3L, "UE1", "et1"))
				.thenReturn(DISPENSE_OK);

		// Mock createDispense doublon
		when(dispenseServiceMock.createDispense(3L, "UE2", "et1"))
				.thenThrow(new DossierException("err.dispense.doublon"));

		// Mock createDispense dossier clos
		when(dispenseServiceMock.createDispense(1L, "UE1", "et1"))
				.thenThrow(new DossierException("err.cours.dossierClos"));

		// Mock addCoursJustificatif OK
		when(dispenseServiceMock.addCoursJustificatif(1L, 10L, "et1"))
				.thenReturn(DISPENSE_AVEC_COURS);

		// Mock deleteDispense OK
		doNothing().when(dispenseServiceMock).deleteDispense(1L, "et1");

		// Mock deleteDispense not found
		doThrow(new DossierException("err.dispense.notFound"))
				.when(dispenseServiceMock).deleteDispense(999L, "et1");

		// Mock removeCoursJustificatif OK
		when(dispenseServiceMock.removeCoursJustificatif(1L, 10L, "et1"))
				.thenReturn(DISPENSE_OK);

		// Mock getDispensesByDossier
		when(dispenseServiceMock.getDispensesByDossier(3L, "et1"))
				.thenReturn(List.of(DISPENSE_OK));
	}

	// ======================== POST /api/dispense/create ========================

	@Test
	void testCreateDispenseOk() throws Exception {
		mockMvc.perform(post("/api/dispense/create")
						.param("dossierId", "3")
						.param("codeUe", "UE1")
						.with(authentication(tokenEt1)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.codeUe").value("UE1"))
				.andExpect(jsonPath("$.nomUe").value("Algorithmique"))
				.andExpect(jsonPath("$.decision").isEmpty())
				.andExpect(jsonPath("$.coursJustificatifs.length()").value(0));
	}

	@Test
	void testCreateDispenseDoublonUe() throws Exception {
		mockMvc.perform(post("/api/dispense/create")
						.param("dossierId", "3")
						.param("codeUe", "UE2")
						.with(authentication(tokenEt1)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error").value(
						"A dispensation already exists for this teaching unit in this file"));
	}

	@Test
	void testCreateDispenseDossierClos() throws Exception {
		mockMvc.perform(post("/api/dispense/create")
						.param("dossierId", "1")
						.param("codeUe", "UE1")
						.with(authentication(tokenEt1)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error").value("File is no longer editable"));
	}

	@Test
	void testCreateDispenseSansAuth() throws Exception {
		mockMvc.perform(post("/api/dispense/create")
						.param("dossierId", "3")
						.param("codeUe", "UE1"))
				.andExpect(status().isUnauthorized());
	}

	// ======================== POST /api/dispense/{id}/cours/{coursId} ========================

	@Test
	void testAddCoursJustificatif() throws Exception {
		mockMvc.perform(post("/api/dispense/1/cours/10")
						.with(authentication(tokenEt1)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.coursJustificatifs.length()").value(1))
				.andExpect(jsonPath("$.coursJustificatifs[0].id").value(10))
				.andExpect(jsonPath("$.coursJustificatifs[0].statutSaisie").value("AUTO_RECONNU"));
	}

	// ======================== DELETE /api/dispense/{id}/cours/{coursId} ========================

	@Test
	void testRemoveCoursJustificatif() throws Exception {
		mockMvc.perform(delete("/api/dispense/1/cours/10")
						.with(authentication(tokenEt1)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.codeUe").value("UE1"))
				.andExpect(jsonPath("$.coursJustificatifs.length()").value(0));
	}

	// ======================== DELETE /api/dispense/{id} ========================

	@Test
	void testDeleteDispense() throws Exception {
		mockMvc.perform(delete("/api/dispense/1")
						.with(authentication(tokenEt1)))
				.andExpect(status().isNoContent());
	}

	@Test
	void testDeleteDispenseNotFound() throws Exception {
		mockMvc.perform(delete("/api/dispense/999")
						.with(authentication(tokenEt1)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error").value("Dispensation not found"));
	}

	// ======================== GET /api/dispense/dossier/{dossierId} ========================

	@Test
	void testGetDispensesByDossier() throws Exception {
		mockMvc.perform(get("/api/dispense/dossier/3")
						.with(authentication(tokenEt1)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].codeUe").value("UE1"));
	}
}
