package org.isfce.pid.controller;

import java.util.Locale;

import org.isfce.pid.controller.error.DossierException;
import org.isfce.pid.dto.DossierDto;
import org.isfce.pid.mapper.UEMapper;
import org.isfce.pid.model.Dossier;
import org.isfce.pid.model.NbDossiers;
import org.isfce.pid.model.User;
import org.isfce.pid.service.DossierService;
import org.isfce.pid.service.UserService;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;

/**
 * Contrôleur REST pour la gestion des dossiers de dispense.
 *
 * @author Ludovic
 */
@RestController
@RequestMapping(path = "/api/dossier/", produces = "application/json")
@Slf4j
@CrossOrigin("*")
public class DossierController {

	DossierService dossierService;

	UserService userService;

	private MessageSource bundle;

	private UEMapper mapper;

	public DossierController(DossierService dossierService, UserService userService, MessageSource bundle,
			UEMapper mapper) {
		super();
		this.dossierService = dossierService;
		this.userService = userService;
		this.bundle = bundle;
		this.mapper = mapper;

	}

	/**
	 * Crée un dossier pour l'étudiant authentifié.
	 * <p>
	 * Applique un lazy provisioning : si l'utilisateur n'existe pas encore en base
	 * (première connexion sans passage par /api/user/profile), il est créé
	 * automatiquement à partir des claims JWT (preferred_username, email,
	 * family_name, given_name) avant de procéder à la création du dossier.
	 * </p>
	 *
	 * @param objetDemande corps de la requête contenant l'objet de la demande
	 * @param locale       locale de la requête pour l'internationalisation des messages
	 * @param auth         token JWT de l'étudiant authentifié
	 * @return le DTO du dossier créé
	 * @throws DossierException      si un dossier est déjà en cours pour cet étudiant
	 * @throws NoSuchMessageException si une clé i18n est introuvable
	 * @author Ludovic
	 */
	@PreAuthorize(value = "hasRole('ETUDIANT')")
	@PostMapping(path = "add", consumes = "application/json")
	public ResponseEntity<DossierDto> nouveauDossier(@Valid @RequestBody ObjetDemande objetDemande, Locale locale,
			JwtAuthenticationToken auth) throws NoSuchMessageException, DossierException {

		String etu = auth.getName();

		// Lazy provisioning : crée l'utilisateur en base s'il n'existe pas encore
		User user = userService.getUserById(etu).orElseGet(() -> {
			var token = auth.getToken();
			String email  = token.getClaimAsString("email");
			String nom    = token.getClaimAsString("family_name");
			String prenom = token.getClaimAsString("given_name");
			log.info("Provisionnement automatique du user '{}'", etu);
			return userService.addUser(new User(etu, email, nom, prenom));
		});

		try {
			Dossier dossier = dossierService.createDossier(user, objetDemande.objetDemande);
			return ResponseEntity.ok(mapper.toDossierDto(dossier));
		} catch (DossierException e) {
			// Le message de l'exception est une clé i18n
			throw new DossierException(bundle.getMessage(e.getMessage(), new String[] {}, locale));
		}
	}

	@PreAuthorize(value = "hasRole('ETUDIANT')")
	@GetMapping("nb")
	public ResponseEntity<NbDossiers> getNbDossiers(JwtAuthenticationToken auth) {
		return ResponseEntity.ok(dossierService.getNbDossiers(auth.getName()));
	}
	
	record ObjetDemande(@NotBlank String objetDemande) {};
}
