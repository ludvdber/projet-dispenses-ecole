package org.isfce.pid.controller;

import java.util.List;
import java.util.Locale;

import org.isfce.pid.controller.error.DossierException;
import org.isfce.pid.dto.AnalyseDto;
import org.isfce.pid.service.AnalyseService;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;

/**
 * Contrôleur REST pour l'analyse de correspondance UE.
 *
 * @author Ludovic
 */
@SuppressWarnings("null")
@RestController
@RequestMapping(path = "/api/analyse/", produces = "application/json")
@AllArgsConstructor
public class AnalyseController {

	private AnalyseService analyseService;
	private MessageSource bundle;

	/**
	 * Retourne les suggestions d'UE ISFCE pour les cours AUTO_RECONNU d'un dossier.
	 */
	@PreAuthorize("hasRole('ETUDIANT')")
	@GetMapping("dossier/{dossierId}")
	public ResponseEntity<List<AnalyseDto>> analyserDossier(
			@PathVariable("dossierId") Long dossierId,
			Locale locale, JwtAuthenticationToken auth) throws NoSuchMessageException, DossierException {
		String username = auth.getToken().getClaimAsString("preferred_username");
		try {
			return ResponseEntity.ok(analyseService.analyserDossier(dossierId, username));
		} catch (DossierException e) {
			throw new DossierException(bundle.getMessage(e.getMessage(), new String[] {}, locale));
		}
	}
}
