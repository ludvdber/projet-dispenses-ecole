package org.isfce.pid.dossier;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import org.isfce.pid.exception.DossierException;
import org.isfce.pid.dto.DossierDto;
import org.isfce.pid.dto.UserDto;
import org.isfce.pid.model.EtatDossier;
import org.isfce.pid.service.DossierService;
import org.isfce.pid.service.UserService;
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
 * Tests MockMvc pour les endpoints GET du DossierController.
 *
 * @author Ludovic
 */
@SuppressWarnings("null")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("testU")
class TestDossierController {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private DossierService dossierServiceMock;

	@MockitoBean
	private UserService userServiceMock;

	private JwtAuthenticationToken tokenEt1;
	private JwtAuthenticationToken tokenEt2;

	private static final UserDto USER_ET1 = new UserDto("et1", "et1@isfce.be", "Nom Et1", "Prénom Et1");

	private static final DossierDto DOSSIER_1 = new DossierDto(
			1L, LocalDate.of(2026, 1, 22), null,
			EtatDossier.DEMANDE_EN_COURS, "Demande de dispense", false, USER_ET1);

	private static final DossierDto DOSSIER_2 = new DossierDto(
			2L, LocalDate.of(2025, 1, 10), LocalDate.of(2025, 1, 20),
			EtatDossier.CLOTURE_ACCORDE, "Ancien dossier", true, USER_ET1);

	@BeforeEach
	void setUp() throws DossierException {
		// Token JWT pour et1 (ETUDIANT)
		Jwt jwtEt1 = Jwt.withTokenValue("token").header("alg", "none")
				.claim("sub", "et1").claim("preferred_username", "et1")
				.claim("given_name", "Prénom Et1").claim("family_name", "Nom Et1")
				.claim("email", "et1@isfce.be").build();
		Collection<GrantedAuthority> authEtu = AuthorityUtils.createAuthorityList("ROLE_ETUDIANT");
		tokenEt1 = new JwtAuthenticationToken(jwtEt1, authEtu);

		// Token JWT pour et2 (ETUDIANT)
		Jwt jwtEt2 = Jwt.withTokenValue("token").header("alg", "none")
				.claim("sub", "et2").claim("preferred_username", "et2")
				.claim("given_name", "Prénom Et2").claim("family_name", "Nom Et2")
				.claim("email", "et2@isfce.be").build();
		tokenEt2 = new JwtAuthenticationToken(jwtEt2, authEtu);

		// Mocks pour getDossiers
		when(dossierServiceMock.getDossiers("et1")).thenReturn(List.of(DOSSIER_1, DOSSIER_2));
		when(dossierServiceMock.getDossiers("et2")).thenReturn(List.of());

		// Mocks pour getDossier
		when(dossierServiceMock.getDossier(1L, "et1")).thenReturn(DOSSIER_1);
		when(dossierServiceMock.getDossier(999L, "et1"))
				.thenThrow(new DossierException("err.dossier.notFound"));
		when(dossierServiceMock.getDossier(1L, "et2"))
				.thenThrow(new DossierException("err.dossier.forbidden"));

		// Mock createDossier OK
		DossierDto newDossier = new DossierDto(
				5L, LocalDate.of(2026, 3, 8), null,
				EtatDossier.DEMANDE_EN_COURS, "Ma demande", false, USER_ET1);
		when(dossierServiceMock.createDossier("Ma demande", "et1")).thenReturn(newDossier);
		when(userServiceMock.existByUsername("et1")).thenReturn(true);

		// Mock createDossier doublon
		when(dossierServiceMock.createDossier("Doublon", "et1"))
				.thenThrow(new DossierException("err.dossier.enCours"));
	}

	// ======================== GET /api/dossier/list ========================

	@Test
	void testGetDossiersEt1() throws Exception {
		mockMvc.perform(get("/api/dossier/list").with(authentication(tokenEt1)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[0].id").value(1))
				.andExpect(jsonPath("$[0].etat").value("DEMANDE_EN_COURS"))
				.andExpect(jsonPath("$[0].objetDemande").value("Demande de dispense"))
				.andExpect(jsonPath("$[1].id").value(2))
				.andExpect(jsonPath("$[1].etat").value("CLOTURE_ACCORDE"));
	}

	@Test
	void testGetDossiersEt2Vide() throws Exception {
		mockMvc.perform(get("/api/dossier/list").with(authentication(tokenEt2)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(0));
	}

	@Test
	void testGetDossiersSansAuth() throws Exception {
		mockMvc.perform(get("/api/dossier/list"))
				.andExpect(status().isUnauthorized());
	}

	// ======================== GET /api/dossier/{id} ========================

	@Test
	void testGetDossierParId() throws Exception {
		mockMvc.perform(get("/api/dossier/1").with(authentication(tokenEt1)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.etat").value("DEMANDE_EN_COURS"))
				.andExpect(jsonPath("$.user.username").value("et1"));
	}

	@Test
	void testGetDossierNotFound() throws Exception {
		mockMvc.perform(get("/api/dossier/999").with(authentication(tokenEt1)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error").value("File not found"));
	}

	@Test
	void testGetDossierForbidden() throws Exception {
		mockMvc.perform(get("/api/dossier/1").with(authentication(tokenEt2)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error").value("Access denied to this file"));
	}

	@Test
	void testGetDossierSansAuth() throws Exception {
		mockMvc.perform(get("/api/dossier/1"))
				.andExpect(status().isUnauthorized());
	}

	// ======================== POST /api/dossier/create ========================

	@Test
	void testCreateDossierOk() throws Exception {
		mockMvc.perform(post("/api/dossier/create")
						.param("objetDemande", "Ma demande")
						.with(authentication(tokenEt1)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(5))
				.andExpect(jsonPath("$.objetDemande").value("Ma demande"))
				.andExpect(jsonPath("$.etat").value("DEMANDE_EN_COURS"))
				.andExpect(jsonPath("$.complet").value(false));
	}

	@Test
	void testCreateDossierDoublon() throws Exception {
		mockMvc.perform(post("/api/dossier/create")
						.param("objetDemande", "Doublon")
						.with(authentication(tokenEt1)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error").value("An administrative file is already open"));
	}

	@Test
	void testCreateDossierSansAuth() throws Exception {
		mockMvc.perform(post("/api/dossier/create")
						.param("objetDemande", "Test"))
				.andExpect(status().isUnauthorized());
	}
}
