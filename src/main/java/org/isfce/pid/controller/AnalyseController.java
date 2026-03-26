package org.isfce.pid.controller;

import java.util.List;

import org.isfce.pid.dto.AnalyseDto;
import org.isfce.pid.service.AnalyseService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

/**
 * Contrôleur REST pour l'analyse de correspondance UE.
 *
 * @author Ludovic
 */
@RestController
@RequestMapping(path = "/api/analyse/", produces = "application/json")
@RequiredArgsConstructor
public class AnalyseController {

	private final AnalyseService analyseService;

	/**
	 * Retourne les suggestions d'UE ISFCE pour les cours AUTO_RECONNU d'un dossier.
	 */
	@PreAuthorize("hasRole('ETUDIANT')")
	@GetMapping("dossier/{dossierId}")
	public ResponseEntity<List<AnalyseDto>> analyserDossier(
			@PathVariable("dossierId") Long dossierId,
			JwtAuthenticationToken auth) {
		String username = auth.getToken().getClaimAsString("preferred_username");
		return ResponseEntity.ok(analyseService.analyserDossier(dossierId, username));
	}
}
