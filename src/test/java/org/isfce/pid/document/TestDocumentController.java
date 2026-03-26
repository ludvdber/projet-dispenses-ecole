package org.isfce.pid.document;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.isfce.pid.exception.DossierException;
import org.isfce.pid.dto.DocumentDto;
import org.isfce.pid.model.TypeDoc;
import org.isfce.pid.service.DocumentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Tests MockMvc pour DocumentController.
 *
 * @author Ludovic
 */
@SuppressWarnings("null")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("testU")
class TestDocumentController {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private DocumentService documentServiceMock;

	private JwtAuthenticationToken tokenEt1;

	private static final LocalDateTime NOW = LocalDateTime.of(2026, 3, 8, 12, 0, 0);

	private static final DocumentDto DOC_OK = new DocumentDto(
			1L, 3L, null, TypeDoc.BULLETIN, "bulletin.pdf",
			"application/pdf", 1024L, NOW, "abc123hash");

	private static final DocumentDto DOC_DELETED = new DocumentDto(
			1L, 3L, null, TypeDoc.BULLETIN, "bulletin.pdf",
			"application/pdf", 1024L, NOW, "abc123hash");

	@BeforeEach
	void setUp() throws DossierException {
		Jwt jwt = Jwt.withTokenValue("token").header("alg", "none")
				.claim("sub", "et1").claim("preferred_username", "et1")
				.claim("given_name", "Prénom Et1").claim("family_name", "Nom Et1")
				.claim("email", "et1@isfce.be").build();
		Collection<GrantedAuthority> auth = AuthorityUtils.createAuthorityList("ROLE_ETUDIANT");
		tokenEt1 = new JwtAuthenticationToken(jwt, auth);

		// Mock uploadDocument PDF valide
		when(documentServiceMock.uploadDocument(eq(3L), isNull(),
				eq(TypeDoc.BULLETIN), any(), eq("et1")))
				.thenReturn(DOC_OK);

		// Mock uploadDocument non-PDF → erreur
		when(documentServiceMock.uploadDocument(eq(3L), isNull(),
				eq(TypeDoc.PROGRAMME_COURS), any(), eq("et1")))
				.thenThrow(new DossierException("err.document.typeMime"));

		// Mock uploadDocument trop lourd → erreur
		when(documentServiceMock.uploadDocument(eq(3L), isNull(),
				eq(TypeDoc.MOTIVATION), any(), eq("et1")))
				.thenThrow(new DossierException("err.document.tailleTrop"));

		// Mock uploadDocument sans cible → erreur
		when(documentServiceMock.uploadDocument(isNull(), isNull(),
				eq(TypeDoc.BULLETIN), any(), eq("et1")))
				.thenThrow(new DossierException("err.document.cibleManquante"));

		// Mock softDeleteDocument
		when(documentServiceMock.softDeleteDocument(1L, "et1"))
				.thenReturn(DOC_DELETED);

		// Mock getDocumentsByDossier
		when(documentServiceMock.getDocumentsByDossier(3L, "et1"))
				.thenReturn(List.of(DOC_OK));

		// Mock downloadDocument (retourne DownloadResult avec resource + métadonnées)
		Resource resource = new ByteArrayResource(new byte[] { 0x25, 0x50, 0x44, 0x46 }); // %PDF
		when(documentServiceMock.downloadDocument(1L, "et1"))
				.thenReturn(new DocumentService.DownloadResult(resource, "application/pdf", "bulletin.pdf"));
	}

	// ======================== POST /api/document/upload ========================

	@Test
	void testUploadPdfValide() throws Exception {
		MockMultipartFile file = new MockMultipartFile(
				"file", "bulletin.pdf", "application/pdf",
				new byte[] { 0x25, 0x50, 0x44, 0x46 });

		mockMvc.perform(multipart("/api/document/upload")
						.file(file)
						.param("dossierId", "3")
						.param("typeDoc", "BULLETIN")
						.with(authentication(tokenEt1)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.typeDoc").value("BULLETIN"))
				.andExpect(jsonPath("$.originalFilename").value("bulletin.pdf"))
				.andExpect(jsonPath("$.typeMime").value("application/pdf"))
				.andExpect(jsonPath("$.hashSha256").value("abc123hash"));
	}

	@Test
	void testUploadNonPdf() throws Exception {
		MockMultipartFile file = new MockMultipartFile(
				"file", "image.png", "image/png",
				new byte[] { 0x00 });

		mockMvc.perform(multipart("/api/document/upload")
						.file(file)
						.param("dossierId", "3")
						.param("typeDoc", "PROGRAMME_COURS")
						.with(authentication(tokenEt1)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error").value("Only PDF files are accepted"));
	}

	@Test
	void testUploadTropLourd() throws Exception {
		MockMultipartFile file = new MockMultipartFile(
				"file", "gros.pdf", "application/pdf",
				new byte[6_000_000]);

		mockMvc.perform(multipart("/api/document/upload")
						.file(file)
						.param("dossierId", "3")
						.param("typeDoc", "MOTIVATION")
						.with(authentication(tokenEt1)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error").value(
						"File exceeds the maximum allowed size"));
	}

	@Test
	void testUploadSansCible() throws Exception {
		MockMultipartFile file = new MockMultipartFile(
				"file", "bulletin.pdf", "application/pdf",
				new byte[] { 0x25, 0x50, 0x44, 0x46 });

		mockMvc.perform(multipart("/api/document/upload")
						.file(file)
						.param("typeDoc", "BULLETIN")
						.with(authentication(tokenEt1)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error").value(
						"A dossierId or coursEtudiantId is required"));
	}

	@Test
	void testUploadSansAuth() throws Exception {
		MockMultipartFile file = new MockMultipartFile(
				"file", "test.pdf", "application/pdf", new byte[] { 0x00 });

		mockMvc.perform(multipart("/api/document/upload")
						.file(file)
						.param("dossierId", "3")
						.param("typeDoc", "BULLETIN"))
				.andExpect(status().isUnauthorized());
	}

	// ======================== DELETE /api/document/{id} ========================

	@Test
	void testSoftDelete() throws Exception {
		mockMvc.perform(delete("/api/document/1")
						.with(authentication(tokenEt1)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.typeDoc").value("BULLETIN"));
	}

	// ======================== GET /api/document/dossier/{dossierId} ========================

	@Test
	void testGetDocumentsByDossier() throws Exception {
		mockMvc.perform(get("/api/document/dossier/3")
						.with(authentication(tokenEt1)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].originalFilename").value("bulletin.pdf"));
	}

	// ======================== GET /api/document/{id}/download ========================

	@Test
	void testDownloadDocument() throws Exception {
		mockMvc.perform(get("/api/document/1/download")
						.with(authentication(tokenEt1)))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_PDF))
				.andExpect(header().string("Content-Disposition",
						"attachment; filename=\"bulletin.pdf\""));
	}
}
