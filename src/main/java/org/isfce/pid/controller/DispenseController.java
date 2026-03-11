package org.isfce.pid.controller;

import java.util.List;
import java.util.Locale;

import org.isfce.pid.controller.error.DossierException;
import org.isfce.pid.dto.DispenseDto;
import org.isfce.pid.service.DispenseService;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
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

import lombok.AllArgsConstructor;

/**
 * Contrôleur REST pour les demandes de dispense.
 *
 * @author Ludovic
 */
@SuppressWarnings("null")
@RestController
@RequestMapping(path = "/api/dispense/", produces = "application/json")
@AllArgsConstructor
public class DispenseController {

	private DispenseService dispenseService;
	private MessageSource bundle;

	/**
	 * Crée une dispense pour une UE dans un dossier.
	 */
	@PreAuthorize("hasRole('ETUDIANT')")
	@PostMapping("create")
	public ResponseEntity<DispenseDto> createDispense(
			@RequestParam("dossierId") Long dossierId,
			@RequestParam("codeUe") String codeUe,
			Locale locale, JwtAuthenticationToken auth) throws NoSuchMessageException, DossierException {
		String username = auth.getToken().getClaimAsString("preferred_username");
		try {
			return ResponseEntity.ok(dispenseService.createDispense(dossierId, codeUe, username));
		} catch (DossierException e) {
			throw new DossierException(bundle.getMessage(e.getMessage(), new String[] {}, locale));
		}
	}

	/**
	 * Supprime une dispense.
	 */
	@PreAuthorize("hasRole('ETUDIANT')")
	@DeleteMapping("{id}")
	public ResponseEntity<Void> deleteDispense(@PathVariable("id") Long id,
			Locale locale, JwtAuthenticationToken auth) throws NoSuchMessageException, DossierException {
		String username = auth.getToken().getClaimAsString("preferred_username");
		try {
			dispenseService.deleteDispense(id, username);
			return ResponseEntity.noContent().build();
		} catch (DossierException e) {
			throw new DossierException(bundle.getMessage(e.getMessage(), new String[] {}, locale));
		}
	}

	/**
	 * Ajoute un cours justificatif à une dispense.
	 */
	@PreAuthorize("hasRole('ETUDIANT')")
	@PostMapping("{id}/cours/{coursId}")
	public ResponseEntity<DispenseDto> addCoursJustificatif(
			@PathVariable("id") Long id,
			@PathVariable("coursId") Long coursId,
			Locale locale, JwtAuthenticationToken auth) throws NoSuchMessageException, DossierException {
		String username = auth.getToken().getClaimAsString("preferred_username");
		try {
			return ResponseEntity.ok(dispenseService.addCoursJustificatif(id, coursId, username));
		} catch (DossierException e) {
			throw new DossierException(bundle.getMessage(e.getMessage(), new String[] {}, locale));
		}
	}

	/**
	 * Retire un cours justificatif d'une dispense.
	 */
	@PreAuthorize("hasRole('ETUDIANT')")
	@DeleteMapping("{id}/cours/{coursId}")
	public ResponseEntity<DispenseDto> removeCoursJustificatif(
			@PathVariable("id") Long id,
			@PathVariable("coursId") Long coursId,
			Locale locale, JwtAuthenticationToken auth) throws NoSuchMessageException, DossierException {
		String username = auth.getToken().getClaimAsString("preferred_username");
		try {
			return ResponseEntity.ok(dispenseService.removeCoursJustificatif(id, coursId, username));
		} catch (DossierException e) {
			throw new DossierException(bundle.getMessage(e.getMessage(), new String[] {}, locale));
		}
	}

	/**
	 * Retourne les dispenses d'un dossier.
	 */
	@PreAuthorize("hasRole('ETUDIANT')")
	@GetMapping("dossier/{dossierId}")
	public ResponseEntity<List<DispenseDto>> getDispensesByDossier(
			@PathVariable("dossierId") Long dossierId,
			Locale locale, JwtAuthenticationToken auth) throws NoSuchMessageException, DossierException {
		String username = auth.getToken().getClaimAsString("preferred_username");
		try {
			return ResponseEntity.ok(dispenseService.getDispensesByDossier(dossierId, username));
		} catch (DossierException e) {
			throw new DossierException(bundle.getMessage(e.getMessage(), new String[] {}, locale));
		}
	}
}
