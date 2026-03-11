package org.isfce.pid.analyse;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collection;
import java.util.List;

import org.isfce.pid.dto.AnalyseDto;
import org.isfce.pid.service.AnalyseService;
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
 * Tests MockMvc pour AnalyseController.
 *
 * @author Ludovic
 */
@SuppressWarnings("null")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("testU")
class TestAnalyseController {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private AnalyseService analyseServiceMock;

	private JwtAuthenticationToken tokenEt1;

	private static final AnalyseDto SUGGESTION_1 = new AnalyseDto(
			"UE1", "Algorithmique", 5, 1, 1, true, java.util.List.of("BINV1010-1 — Principes algorithmiques"));

	@BeforeEach
	void setUp() throws Exception {
		Jwt jwt = Jwt.withTokenValue("token").header("alg", "none")
				.claim("sub", "et1").claim("preferred_username", "et1")
				.claim("given_name", "Prénom Et1").claim("family_name", "Nom Et1")
				.claim("email", "et1@isfce.be").build();
		Collection<GrantedAuthority> auth = AuthorityUtils.createAuthorityList("ROLE_ETUDIANT");
		tokenEt1 = new JwtAuthenticationToken(jwt, auth);

		// Dossier 3 : 1 cours AUTO_RECONNU → 1 suggestion
		when(analyseServiceMock.analyserDossier(3L, "et1"))
				.thenReturn(List.of(SUGGESTION_1));

		// Dossier 4 : UE déjà couverte par dispense → liste vide
		when(analyseServiceMock.analyserDossier(4L, "et1"))
				.thenReturn(List.of());

		// Dossier 5 : cours INCONNU uniquement → liste vide
		when(analyseServiceMock.analyserDossier(5L, "et1"))
				.thenReturn(List.of());
	}

	@Test
	void testAnalyseAutoReconnu() throws Exception {
		mockMvc.perform(get("/api/analyse/dossier/3")
						.with(authentication(tokenEt1)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].codeUe").value("UE1"))
				.andExpect(jsonPath("$[0].nomUe").value("Algorithmique"))
				.andExpect(jsonPath("$[0].credits").value(5))
				.andExpect(jsonPath("$[0].coursPresents").value(1))
				.andExpect(jsonPath("$[0].coursRequis").value(1))
				.andExpect(jsonPath("$[0].complet").value(true));
	}

	@Test
	void testAnalyseUeDejaDispensee() throws Exception {
		mockMvc.perform(get("/api/analyse/dossier/4")
						.with(authentication(tokenEt1)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(0));
	}

	@Test
	void testAnalyseCoursInconnu() throws Exception {
		mockMvc.perform(get("/api/analyse/dossier/5")
						.with(authentication(tokenEt1)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(0));
	}

	@Test
	void testAnalyseSansAuth() throws Exception {
		mockMvc.perform(get("/api/analyse/dossier/3"))
				.andExpect(status().isUnauthorized());
	}
}
