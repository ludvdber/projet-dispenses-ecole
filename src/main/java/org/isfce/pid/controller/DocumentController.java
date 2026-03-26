package org.isfce.pid.controller;

import java.util.List;

import org.isfce.pid.dto.DocumentDto;
import org.isfce.pid.model.TypeDoc;
import org.isfce.pid.service.DocumentService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
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

import lombok.RequiredArgsConstructor;

/**
 * Contrôleur REST pour l'upload et la gestion des documents.
 *
 * @author Ludovic
 */
@SuppressWarnings("null")
@RestController
@RequestMapping(path = "/api/document/", produces = "application/json")
@RequiredArgsConstructor
public class DocumentController {

	private final DocumentService documentService;

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
			JwtAuthenticationToken auth) {
		String username = auth.getToken().getClaimAsString("preferred_username");
		return ResponseEntity.status(HttpStatus.CREATED).body(documentService.uploadDocument(
				dossierId, coursEtudiantId, typeDoc, file, username));
	}

	/**
	 * Soft-delete un document.
	 */
	@PreAuthorize("hasRole('ETUDIANT')")
	@DeleteMapping("{id}")
	public ResponseEntity<DocumentDto> softDeleteDocument(@PathVariable("id") Long id,
			JwtAuthenticationToken auth) {
		String username = auth.getToken().getClaimAsString("preferred_username");
		return ResponseEntity.ok(documentService.softDeleteDocument(id, username));
	}

	/**
	 * Liste les documents actifs d'un dossier.
	 */
	@PreAuthorize("hasRole('ETUDIANT')")
	@GetMapping("dossier/{dossierId}")
	public ResponseEntity<List<DocumentDto>> getDocumentsByDossier(
			@PathVariable("dossierId") Long dossierId,
			JwtAuthenticationToken auth) {
		String username = auth.getToken().getClaimAsString("preferred_username");
		return ResponseEntity.ok(documentService.getDocumentsByDossier(dossierId, username));
	}

	/**
	 * Télécharge un document (streaming).
	 */
	@PreAuthorize("hasRole('ETUDIANT')")
	@GetMapping("{id}/download")
	public ResponseEntity<Resource> downloadDocument(@PathVariable("id") Long id,
			JwtAuthenticationToken auth) {
		String username = auth.getToken().getClaimAsString("preferred_username");
		var result = documentService.downloadDocument(id, username);
		return ResponseEntity.ok()
				.contentType(MediaType.parseMediaType(result.typeMime()))
				.header(HttpHeaders.CONTENT_DISPOSITION,
						"attachment; filename=\"" + result.originalFilename() + "\"")
				.body(result.resource());
	}
}
