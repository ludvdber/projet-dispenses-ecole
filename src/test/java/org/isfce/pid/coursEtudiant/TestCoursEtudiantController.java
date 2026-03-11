package org.isfce.pid.coursEtudiant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

import org.isfce.pid.controller.error.DossierException;
import org.isfce.pid.dto.CoursEtudiantDto;
import org.isfce.pid.model.StatutSaisie;
import org.isfce.pid.service.CoursEtudiantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Tests MockMvc pour CoursEtudiantController.
 *
 * @author Ludovic
 */
@SuppressWarnings("null")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("testU")
class TestCoursEtudiantController {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private CoursEtudiantService coursEtudiantServiceMock;

	private JwtAuthenticationToken tokenEt1;

	// --- Réponses mockées ---

	private static final CoursEtudiantDto COURS_AUTO = new CoursEtudiantDto(
			1L, 3L, "LDV", null, "BINV1010-1", "Programmation Java avancée",
			6, null, 1L, StatutSaisie.AUTO_RECONNU);

	private static final CoursEtudiantDto COURS_INCONNU_ECOLE_CONNUE = new CoursEtudiantDto(
			2L, 3L, "LDV", null, "NEW101", "Cours inconnu",
			5, null, null, StatutSaisie.INCONNU);

	private static final CoursEtudiantDto COURS_ECOLE_INCONNUE = new CoursEtudiantDto(
			3L, 3L, null, "Autre école", "PROG101", "Introduction",
			5, null, null, StatutSaisie.INCONNU);

	@BeforeEach
	void setUp() throws DossierException {
		Jwt jwt = Jwt.withTokenValue("token").header("alg", "none")
				.claim("sub", "et1").claim("preferred_username", "et1")
				.claim("given_name", "Prénom Et1").claim("family_name", "Nom Et1")
				.claim("email", "et1@isfce.be").build();
		Collection<GrantedAuthority> auth = AuthorityUtils.createAuthorityList("ROLE_ETUDIANT");
		tokenEt1 = new JwtAuthenticationToken(jwt, auth);

		// Mock addCours : école connue + cours reconnu → AUTO_RECONNU
		when(coursEtudiantServiceMock.addCours(eq(3L), any(), eq("et1")))
				.thenAnswer(invocation -> {
					CoursEtudiantDto input = invocation.getArgument(1);
					if ("LDV".equals(input.codeEcole()) && "BINV1010-1".equals(input.codeCours())) {
						return COURS_AUTO;
					} else if ("LDV".equals(input.codeEcole()) && "NEW101".equals(input.codeCours())) {
						return COURS_INCONNU_ECOLE_CONNUE;
					} else {
						return COURS_ECOLE_INCONNUE;
					}
				});

		// Mock addCours : dossier soumis → erreur
		when(coursEtudiantServiceMock.addCours(eq(1L), any(), eq("et1")))
				.thenThrow(new DossierException("err.cours.dossierClos"));

		// Mock getCoursByDossier
		when(coursEtudiantServiceMock.getCoursByDossier(3L, "et1"))
				.thenReturn(List.of(COURS_AUTO, COURS_ECOLE_INCONNUE));

		// Mock deleteCours
		doNothing().when(coursEtudiantServiceMock).deleteCours(1L, "et1");
		doThrow(new DossierException("err.cours.notFound"))
				.when(coursEtudiantServiceMock).deleteCours(999L, "et1");
	}

	// ======================== POST /api/cours-etudiant/add ========================

	@Test
	void testAddCoursAutoReconnu() throws Exception {
		CoursEtudiantDto input = new CoursEtudiantDto(
				null, 3L, "LDV", null, "BINV1010-1", "Programmation Java avancée",
				6, null, null, null);

		mockMvc.perform(post("/api/cours-etudiant/add")
						.with(authentication(tokenEt1))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(input)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.statutSaisie").value("AUTO_RECONNU"))
				.andExpect(jsonPath("$.corrCoursId").value(1))
				.andExpect(jsonPath("$.codeEcole").value("LDV"));
	}

	@Test
	void testAddCoursEcoleConnueCoursPasReconnu() throws Exception {
		CoursEtudiantDto input = new CoursEtudiantDto(
				null, 3L, "LDV", null, "NEW101", "Cours inconnu",
				5, null, null, null);

		mockMvc.perform(post("/api/cours-etudiant/add")
						.with(authentication(tokenEt1))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(input)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.statutSaisie").value("INCONNU"))
				.andExpect(jsonPath("$.corrCoursId").isEmpty())
				.andExpect(jsonPath("$.codeEcole").value("LDV"));
	}

	@Test
	void testAddCoursEcoleInconnue() throws Exception {
		CoursEtudiantDto input = new CoursEtudiantDto(
				null, 3L, null, "Autre école", "PROG101", "Introduction",
				5, null, null, null);

		mockMvc.perform(post("/api/cours-etudiant/add")
						.with(authentication(tokenEt1))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(input)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.statutSaisie").value("INCONNU"))
				.andExpect(jsonPath("$.nomEcole").value("Autre école"))
				.andExpect(jsonPath("$.codeEcole").isEmpty());
	}

	@Test
	void testAddCoursDossierSoumis() throws Exception {
		CoursEtudiantDto input = new CoursEtudiantDto(
				null, 1L, "LDV", null, "BINV1010-1", "Test",
				6, null, null, null);

		mockMvc.perform(post("/api/cours-etudiant/add")
						.with(authentication(tokenEt1))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(input)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error").value("File is no longer editable"));
	}

	@Test
	void testAddCoursSansAuth() throws Exception {
		mockMvc.perform(post("/api/cours-etudiant/add")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{}"))
				.andExpect(status().isUnauthorized());
	}

	// ======================== GET /api/cours-etudiant/dossier/{id} ========================

	@Test
	void testGetCoursByDossier() throws Exception {
		mockMvc.perform(get("/api/cours-etudiant/dossier/3")
						.with(authentication(tokenEt1)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[0].statutSaisie").value("AUTO_RECONNU"))
				.andExpect(jsonPath("$[1].nomEcole").value("Autre école"));
	}

	// ======================== DELETE /api/cours-etudiant/{id} ========================

	@Test
	void testDeleteCours() throws Exception {
		mockMvc.perform(delete("/api/cours-etudiant/1")
						.with(authentication(tokenEt1)))
				.andExpect(status().isNoContent());
	}

	@Test
	void testDeleteCoursNotFound() throws Exception {
		mockMvc.perform(delete("/api/cours-etudiant/999")
						.with(authentication(tokenEt1)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error").value("Course not found"));
	}
}
