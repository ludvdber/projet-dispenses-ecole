package org.isfce.pid.controller;

import java.util.List;
import java.util.Locale;

import org.isfce.pid.controller.error.DossierException;
import org.isfce.pid.dto.DocumentDto;
import org.isfce.pid.model.TypeDoc;
import org.isfce.pid.service.DocumentService;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;

/**
 * Contrôleur REST pour l'upload et la gestion des documents.
 *
 * @author Ludovic
 */
@SuppressWarnings("null")
@RestController
@RequestMapping(path = "/api/document/", produces = "application/json")
@AllArgsConstructor
public class DocumentController {

	private DocumentService documentService;
	private MessageSource bundle;

	/**
	 * Upload un document (multipart/form-data).
	 */
	@PreAuthorize("hasRole('ETUDIANT')")
	@PostMapping(path = "upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<DocumentDto> uploadDocument(
			@RequestParam(value = "dossierId", required = false) Long dossierId,
			@RequestParam(value = "coursEtudiantId", required = false) Long coursEtudiantId,
			@RequestParam("typeDoc") TypeDoc typeDoc,
			@RequestParam("file") MultipartFile file,
			Locale locale, JwtAuthenticationToken auth) throws NoSuchMessageException, DossierException {
		String username = auth.getToken().getClaimAsString("preferred_username");
		try {
			return ResponseEntity.ok(documentService.uploadDocument(
					dossierId, coursEtudiantId, typeDoc, file, username));
		} catch (DossierException e) {
			throw new DossierException(bundle.getMessage(e.getMessage(), new String[] {}, locale));
		}
	}

	/**
	 * Soft-delete un document.
	 */
	@PreAuthorize("hasRole('ETUDIANT')")
	@DeleteMapping("{id}")
	public ResponseEntity<DocumentDto> softDeleteDocument(@PathVariable("id") Long id,
			Locale locale, JwtAuthenticationToken auth) throws NoSuchMessageException, DossierException {
		String username = auth.getToken().getClaimAsString("preferred_username");
		try {
			return ResponseEntity.ok(documentService.softDeleteDocument(id, username));
		} catch (DossierException e) {
			throw new DossierException(bundle.getMessage(e.getMessage(), new String[] {}, locale));
		}
	}

	/**
	 * Liste les documents actifs d'un dossier.
	 */
	@PreAuthorize("hasRole('ETUDIANT')")
	@GetMapping("dossier/{dossierId}")
	public ResponseEntity<List<DocumentDto>> getDocumentsByDossier(
			@PathVariable("dossierId") Long dossierId,
			Locale locale, JwtAuthenticationToken auth) throws NoSuchMessageException, DossierException {
		String username = auth.getToken().getClaimAsString("preferred_username");
		try {
			return ResponseEntity.ok(documentService.getDocumentsByDossier(dossierId, username));
		} catch (DossierException e) {
			throw new DossierException(bundle.getMessage(e.getMessage(), new String[] {}, locale));
		}
	}

	/**
	 * Télécharge un document (streaming).
	 */
	@PreAuthorize("hasRole('ETUDIANT')")
	@GetMapping("{id}/download")
	public ResponseEntity<Resource> downloadDocument(@PathVariable("id") Long id,
			Locale locale, JwtAuthenticationToken auth) throws NoSuchMessageException, DossierException {
		String username = auth.getToken().getClaimAsString("preferred_username");
		try {
			var result = documentService.downloadDocument(id, username);
			return ResponseEntity.ok()
					.contentType(MediaType.parseMediaType(result.typeMime()))
					.header(HttpHeaders.CONTENT_DISPOSITION,
							"attachment; filename=\"" + result.originalFilename() + "\"")
					.body(result.resource());
		} catch (DossierException e) {
			throw new DossierException(bundle.getMessage(e.getMessage(), new String[] {}, locale));
		}
	}
}
