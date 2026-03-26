package org.isfce.pid.controller;

import java.util.List;

import org.isfce.pid.dto.CompletudeDossier;
import org.isfce.pid.dto.DossierDto;
import org.isfce.pid.service.DossierService;
import org.isfce.pid.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Contrôleur REST pour la gestion des dossiers de dispense.
 *
 * @author Ludovic
 */
@RestController
@RequestMapping(path = "/api/dossier/", produces = "application/json")
@RequiredArgsConstructor
@Slf4j
public class DossierController {

	private final DossierService dossierService;
	private final UserService userService;

	/**
	 * Crée un dossier pour l'étudiant authentifié.
	 */
	@PreAuthorize(value = "hasRole('ETUDIANT')")
	@PostMapping("create")
	public ResponseEntity<DossierDto> createDossier(@RequestParam("objetDemande") String objetDemande,
			JwtAuthenticationToken auth) {
		userService.provisionFromJwt(auth);
		String etu = auth.getToken().getClaimAsString("preferred_username");
		return ResponseEntity.status(HttpStatus.CREATED).body(dossierService.createDossier(objetDemande, etu));
	}

	@PreAuthorize(value = "hasRole('ETUDIANT')")
	@GetMapping("list")
	public ResponseEntity<List<DossierDto>> getDossiers(JwtAuthenticationToken auth) {
		String username = auth.getToken().getClaimAsString("preferred_username");
		return ResponseEntity.ok(dossierService.getDossiers(username));
	}

	@PreAuthorize(value = "hasRole('ETUDIANT')")
	@GetMapping("{id}")
	public ResponseEntity<DossierDto> getDossier(@PathVariable("id") Long id,
			JwtAuthenticationToken auth) {
		String username = auth.getToken().getClaimAsString("preferred_username");
		return ResponseEntity.ok(dossierService.getDossier(id, username));
	}

	@PreAuthorize(value = "hasRole('ETUDIANT')")
	@GetMapping("{id}/completude")
	public ResponseEntity<CompletudeDossier> checkCompletude(@PathVariable("id") Long id,
			JwtAuthenticationToken auth) {
		String username = auth.getToken().getClaimAsString("preferred_username");
		// Vérifie que le dossier appartient bien à l'étudiant  
		dossierService.getDossier(id, username);
		return ResponseEntity.ok(dossierService.checkCompletude(id));
	}

	@PreAuthorize(value = "hasRole('ETUDIANT')")
	@PutMapping("{id}/submit")
	public ResponseEntity<DossierDto> submitDossier(@PathVariable("id") Long id,
			JwtAuthenticationToken auth) {
		String username = auth.getToken().getClaimAsString("preferred_username");
		return ResponseEntity.ok(dossierService.submitDossier(id, username));
	}
}
