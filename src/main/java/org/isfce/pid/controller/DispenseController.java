package org.isfce.pid.controller;

import java.util.List;

import org.isfce.pid.dto.DispenseDto;
import org.isfce.pid.service.DispenseService;
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

import lombok.RequiredArgsConstructor;

/**
 * Contrôleur REST pour les demandes de dispense.
 *
 * @author Ludovic
 */
@RestController
@RequestMapping(path = "/api/dispense/", produces = "application/json")
@RequiredArgsConstructor
public class DispenseController {

	private final DispenseService dispenseService;

	/**
	 * Crée une dispense pour une UE dans un dossier.
	 */
	@PreAuthorize("hasRole('ETUDIANT')")
	@PostMapping("create")
	public ResponseEntity<DispenseDto> createDispense(
			@RequestParam("dossierId") Long dossierId,
			@RequestParam("codeUe") String codeUe,
			JwtAuthenticationToken auth) {
		String username = auth.getToken().getClaimAsString("preferred_username");
		return ResponseEntity.status(HttpStatus.CREATED).body(dispenseService.createDispense(dossierId, codeUe, username));
	}

	/**
	 * Supprime une dispense.
	 */
	@PreAuthorize("hasRole('ETUDIANT')")
	@DeleteMapping("{id}")
	public ResponseEntity<Void> deleteDispense(@PathVariable("id") Long id,
			JwtAuthenticationToken auth) {
		String username = auth.getToken().getClaimAsString("preferred_username");
		dispenseService.deleteDispense(id, username);
		return ResponseEntity.noContent().build();
	}

	/**
	 * Ajoute un cours justificatif à une dispense.
	 */
	@PreAuthorize("hasRole('ETUDIANT')")
	@PostMapping("{id}/cours/{coursId}")
	public ResponseEntity<DispenseDto> addCoursJustificatif(
			@PathVariable("id") Long id,
			@PathVariable("coursId") Long coursId,
			JwtAuthenticationToken auth) {
		String username = auth.getToken().getClaimAsString("preferred_username");
		return ResponseEntity.ok(dispenseService.addCoursJustificatif(id, coursId, username));
	}

	/**
	 * Retire un cours justificatif d'une dispense.
	 */
	@PreAuthorize("hasRole('ETUDIANT')")
	@DeleteMapping("{id}/cours/{coursId}")
	public ResponseEntity<DispenseDto> removeCoursJustificatif(
			@PathVariable("id") Long id,
			@PathVariable("coursId") Long coursId,
			JwtAuthenticationToken auth) {
		String username = auth.getToken().getClaimAsString("preferred_username");
		return ResponseEntity.ok(dispenseService.removeCoursJustificatif(id, coursId, username));
	}

	/**
	 * Retourne les dispenses d'un dossier.
	 */
	@PreAuthorize("hasRole('ETUDIANT')")
	@GetMapping("dossier/{dossierId}")
	public ResponseEntity<List<DispenseDto>> getDispensesByDossier(
			@PathVariable("dossierId") Long dossierId,
			JwtAuthenticationToken auth) {
		String username = auth.getToken().getClaimAsString("preferred_username");
		return ResponseEntity.ok(dispenseService.getDispensesByDossier(dossierId, username));
	}
}
