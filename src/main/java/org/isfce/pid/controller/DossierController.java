package org.isfce.pid.controller;

import java.util.List;
import java.util.Locale;

import org.isfce.pid.controller.error.DossierException;
import org.isfce.pid.dto.CompletudeDossier;
import org.isfce.pid.dto.DossierDto;
import org.isfce.pid.model.User;
import org.isfce.pid.service.DossierService;
import org.isfce.pid.service.UserService;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
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

import lombok.extern.slf4j.Slf4j;

/**
 * Contrôleur REST pour la gestion des dossiers de dispense.
 *
 * @author Ludovic
 */
@SuppressWarnings("null")
@RestController
@RequestMapping(path = "/api/dossier/", produces = "application/json")
@Slf4j
public class DossierController {

	DossierService dossierService;

	UserService userService;

	private MessageSource bundle;

	public DossierController(DossierService dossierService, UserService userService, MessageSource bundle) {
		super();
		this.dossierService = dossierService;
		this.userService = userService;
		this.bundle = bundle;
	}

	/**
	 * Crée un dossier pour l'étudiant authentifié.
	 * Lazy provisioning du user si nécessaire.
	 */
	@PreAuthorize(value = "hasRole('ETUDIANT')")
	@PostMapping("create")
	public ResponseEntity<DossierDto> createDossier(@RequestParam("objetDemande") String objetDemande,
			Locale locale, JwtAuthenticationToken auth) throws NoSuchMessageException, DossierException {
		String etu = auth.getToken().getClaimAsString("preferred_username");

		// Lazy provisioning
		if (!userService.existByUsername(etu)) {
			var token = auth.getToken();
			String email = token.getClaimAsString("email");
			String nom = token.getClaimAsString("family_name");
			String prenom = token.getClaimAsString("given_name");
			log.info("Provisionnement automatique du user '{}'", etu);
			userService.addUser(new User(etu, email, nom, prenom));
		}

		try {
			return ResponseEntity.ok(dossierService.createDossier(objetDemande, etu));
		} catch (DossierException e) {
			throw new DossierException(bundle.getMessage(e.getMessage(), new String[] {}, locale));
		}
	}

	@PreAuthorize(value = "hasRole('ETUDIANT')")
	@GetMapping("list")
	public ResponseEntity<List<DossierDto>> getDossiers(JwtAuthenticationToken auth) {
		String username = auth.getToken().getClaimAsString("preferred_username");
		return ResponseEntity.ok(dossierService.getDossiers(username));
	}

	@PreAuthorize(value = "hasRole('ETUDIANT')")
	@GetMapping("{id}")
	public ResponseEntity<DossierDto> getDossier(@PathVariable("id") Long id, Locale locale,
			JwtAuthenticationToken auth) throws DossierException {
		String username = auth.getToken().getClaimAsString("preferred_username");
		try {
			return ResponseEntity.ok(dossierService.getDossier(id, username));
		} catch (DossierException e) {
			throw new DossierException(bundle.getMessage(e.getMessage(), new String[] {}, locale));
		}
	}

	@PreAuthorize(value = "hasRole('ETUDIANT')")
	@GetMapping("{id}/completude")
	public ResponseEntity<CompletudeDossier> checkCompletude(@PathVariable("id") Long id, Locale locale,
			JwtAuthenticationToken auth) throws DossierException {
		String username = auth.getToken().getClaimAsString("preferred_username");
		try {
			// Vérifie ownership
			dossierService.getDossier(id, username);
			return ResponseEntity.ok(dossierService.checkCompletude(id));
		} catch (DossierException e) {
			throw new DossierException(bundle.getMessage(e.getMessage(), new String[] {}, locale));
		}
	}

	@PreAuthorize(value = "hasRole('ETUDIANT')")
	@PutMapping("{id}/submit")
	public ResponseEntity<DossierDto> submitDossier(@PathVariable("id") Long id, Locale locale,
			JwtAuthenticationToken auth) throws DossierException {
		String username = auth.getToken().getClaimAsString("preferred_username");
		try {
			return ResponseEntity.ok(dossierService.submitDossier(id, username));
		} catch (DossierException e) {
			throw new DossierException(bundle.getMessage(e.getMessage(), new String[] {}, locale));
		}
	}
}
