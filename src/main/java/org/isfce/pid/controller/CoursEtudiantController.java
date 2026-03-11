package org.isfce.pid.controller;

import java.util.List;
import java.util.Locale;

import org.isfce.pid.controller.error.DossierException;
import org.isfce.pid.dto.CoursEtudiantDto;
import org.isfce.pid.service.CoursEtudiantService;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

/**
 * Contrôleur REST pour les cours étudiants d'un dossier de dispense.
 *
 * @author Ludovic
 */
@SuppressWarnings("null")
@RestController
@RequestMapping(path = "/api/cours-etudiant/", produces = "application/json")
@AllArgsConstructor
public class CoursEtudiantController {

	private CoursEtudiantService coursEtudiantService;
	private MessageSource bundle;

	@PreAuthorize("hasRole('ETUDIANT')")
	@PostMapping(path = "add", consumes = "application/json")
	public ResponseEntity<CoursEtudiantDto> addCours(@Valid @RequestBody CoursEtudiantDto dto,
			Locale locale, JwtAuthenticationToken auth) throws NoSuchMessageException, DossierException {
		String username = auth.getToken().getClaimAsString("preferred_username");
		try {
			return ResponseEntity.ok(coursEtudiantService.addCours(dto.dossierId(), dto, username));
		} catch (DossierException e) {
			throw new DossierException(bundle.getMessage(e.getMessage(), new String[] {}, locale));
		}
	}

	@PreAuthorize("hasRole('ETUDIANT')")
	@DeleteMapping("{id}")
	public ResponseEntity<Void> deleteCours(@PathVariable("id") Long id, Locale locale,
			JwtAuthenticationToken auth) throws NoSuchMessageException, DossierException {
		String username = auth.getToken().getClaimAsString("preferred_username");
		try {
			coursEtudiantService.deleteCours(id, username);
			return ResponseEntity.noContent().build();
		} catch (DossierException e) {
			throw new DossierException(bundle.getMessage(e.getMessage(), new String[] {}, locale));
		}
	}

	@PreAuthorize("hasRole('ETUDIANT')")
	@GetMapping("dossier/{dossierId}")
	public ResponseEntity<List<CoursEtudiantDto>> getCoursByDossier(@PathVariable("dossierId") Long dossierId,
			Locale locale, JwtAuthenticationToken auth) throws NoSuchMessageException, DossierException {
		String username = auth.getToken().getClaimAsString("preferred_username");
		try {
			return ResponseEntity.ok(coursEtudiantService.getCoursByDossier(dossierId, username));
		} catch (DossierException e) {
			throw new DossierException(bundle.getMessage(e.getMessage(), new String[] {}, locale));
		}
	}
}
