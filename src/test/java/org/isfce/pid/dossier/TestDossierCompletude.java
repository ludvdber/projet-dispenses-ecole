package org.isfce.pid.dossier;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.Collection;

import org.isfce.pid.exception.DossierException;
import org.isfce.pid.dto.CompletudeDossier;
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
 * Tests MockMvc pour la complétude et soumission de dossier.
 *
 * @author Ludovic
 */
@SuppressWarnings("null")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("testU")
class TestDossierCompletude {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private DossierService dossierServiceMock;

	@MockitoBean
	private UserService userServiceMock;

	private JwtAuthenticationToken tokenEt1;

	private static final UserDto USER_ET1 = new UserDto("et1", "et1@isfce.be", "Nom Et1", "Prénom Et1");

	private static final DossierDto DOSSIER_EN_COURS = new DossierDto(
			3L, LocalDate.of(2026, 1, 22), null,
			EtatDossier.DEMANDE_EN_COURS, "Demande de dispense", false, USER_ET1);

	private static final DossierDto DOSSIER_SOUMIS = new DossierDto(
			3L, LocalDate.of(2026, 1, 22), LocalDate.of(2026, 3, 8),
			EtatDossier.TRAITEMENT_DIRECTION, "Demande de dispense", true, USER_ET1);

	@BeforeEach
	void setUp() throws DossierException {
		Jwt jwt = Jwt.withTokenValue("token").header("alg", "none")
				.claim("sub", "et1").claim("preferred_username", "et1")
				.claim("given_name", "Prénom Et1").claim("family_name", "Nom Et1")
				.claim("email", "et1@isfce.be").build();
		Collection<GrantedAuthority> auth = AuthorityUtils.createAuthorityList("ROLE_ETUDIANT");
		tokenEt1 = new JwtAuthenticationToken(jwt, auth);

		// Mock getDossier pour vérification ownership dans checkCompletude
		when(dossierServiceMock.getDossier(3L, "et1")).thenReturn(DOSSIER_EN_COURS);

		// --- Complétude : bulletin manquant ---
		when(dossierServiceMock.checkCompletude(10L))
				.thenReturn(new CompletudeDossier(false, false, true, true, true, false));
		when(dossierServiceMock.getDossier(10L, "et1")).thenReturn(
				new DossierDto(10L, LocalDate.of(2026, 1, 1), null,
						EtatDossier.DEMANDE_EN_COURS, "Test", false, USER_ET1));

		// --- Complétude : motivation manquante ---
		when(dossierServiceMock.checkCompletude(11L))
				.thenReturn(new CompletudeDossier(false, true, false, true, true, false));
		when(dossierServiceMock.getDossier(11L, "et1")).thenReturn(
				new DossierDto(11L, LocalDate.of(2026, 1, 1), null,
						EtatDossier.DEMANDE_EN_COURS, "Test", false, USER_ET1));

		// --- Complétude : aucune dispense ---
		when(dossierServiceMock.checkCompletude(12L))
				.thenReturn(new CompletudeDossier(false, true, true, false, true, false));
		when(dossierServiceMock.getDossier(12L, "et1")).thenReturn(
				new DossierDto(12L, LocalDate.of(2026, 1, 1), null,
						EtatDossier.DEMANDE_EN_COURS, "Test", false, USER_ET1));

		// --- Complétude : cours INCONNU sans URL ni programme ---
		when(dossierServiceMock.checkCompletude(13L))
				.thenReturn(new CompletudeDossier(false, true, true, true, false, true));
		when(dossierServiceMock.getDossier(13L, "et1")).thenReturn(
				new DossierDto(13L, LocalDate.of(2026, 1, 1), null,
						EtatDossier.DEMANDE_EN_COURS, "Test", false, USER_ET1));

		// --- Complétude OK ---
		when(dossierServiceMock.checkCompletude(3L))
				.thenReturn(new CompletudeDossier(true, true, true, true, true, false));

		// --- Submit OK ---
		when(dossierServiceMock.submitDossier(3L, "et1")).thenReturn(DOSSIER_SOUMIS);

		// --- Submit incomplet ---
		when(dossierServiceMock.submitDossier(10L, "et1"))
				.thenThrow(new DossierException("err.dossier.incomplet"));

		// --- Submit déjà en traitement ---
		when(dossierServiceMock.submitDossier(20L, "et1"))
				.thenThrow(new DossierException("err.dossier.dejaEnTraitement"));
		when(dossierServiceMock.getDossier(20L, "et1")).thenReturn(
				new DossierDto(20L, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 2, 1),
						EtatDossier.TRAITEMENT_DIRECTION, "Test", true, USER_ET1));
	}

	// ======================== GET /api/dossier/{id}/completude ========================

	@Test
	void testCompletudeBulletinManquant() throws Exception {
		mockMvc.perform(get("/api/dossier/10/completude")
						.with(authentication(tokenEt1)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.complet").value(false))
				.andExpect(jsonPath("$.bulletinOk").value(false))
				.andExpect(jsonPath("$.motivationOk").value(true));
	}

	@Test
	void testCompletudeMotivationManquante() throws Exception {
		mockMvc.perform(get("/api/dossier/11/completude")
						.with(authentication(tokenEt1)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.complet").value(false))
				.andExpect(jsonPath("$.motivationOk").value(false));
	}

	@Test
	void testCompletudeAucuneDispense() throws Exception {
		mockMvc.perform(get("/api/dossier/12/completude")
						.with(authentication(tokenEt1)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.complet").value(false))
				.andExpect(jsonPath("$.dispensesOk").value(false));
	}

	@Test
	void testCompletudeCoursInconnuSansUrlNiProgramme() throws Exception {
		mockMvc.perform(get("/api/dossier/13/completude")
						.with(authentication(tokenEt1)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.complet").value(false))
				.andExpect(jsonPath("$.coursInconnusOk").value(false));
	}

	// ======================== PUT /api/dossier/{id}/submit ========================

	@Test
	void testSubmitDossierOk() throws Exception {
		mockMvc.perform(put("/api/dossier/3/submit")
						.with(authentication(tokenEt1)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.etat").value("TRAITEMENT_DIRECTION"))
				.andExpect(jsonPath("$.complet").value(true))
				.andExpect(jsonPath("$.dateSoumis").value("2026-03-08"));
	}

	@Test
	void testSubmitDossierIncomplet() throws Exception {
		mockMvc.perform(put("/api/dossier/10/submit")
						.with(authentication(tokenEt1)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error").value(
						"The file is incomplete and cannot be submitted"));
	}

	@Test
	void testSubmitDossierDejaEnTraitement() throws Exception {
		mockMvc.perform(put("/api/dossier/20/submit")
						.with(authentication(tokenEt1)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error").value(
						"The file is already being processed"));
	}

	@Test
	void testSubmitSansAuth() throws Exception {
		mockMvc.perform(put("/api/dossier/3/submit"))
				.andExpect(status().isUnauthorized());
	}
}
