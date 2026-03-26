package org.isfce.pid.controller;

import java.util.List;

import org.isfce.pid.dto.CoursEtudiantDto;
import org.isfce.pid.service.CoursEtudiantService;
import org.springframework.http.HttpStatus;
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
import lombok.RequiredArgsConstructor;

/**
 * Contrôleur REST pour les cours étudiants d'un dossier de dispense.
 *
 * @author Ludovic
 */
@SuppressWarnings("null")
@RestController
@RequestMapping(path = "/api/cours-etudiant/", produces = "application/json")
@RequiredArgsConstructor
public class CoursEtudiantController {

	private final CoursEtudiantService coursEtudiantService;

	@PreAuthorize("hasRole('ETUDIANT')")
	@PostMapping(path = "add", consumes = "application/json")
	public ResponseEntity<CoursEtudiantDto> addCours(@Valid @RequestBody CoursEtudiantDto dto,
			JwtAuthenticationToken auth) {
		String username = auth.getToken().getClaimAsString("preferred_username");
		return ResponseEntity.status(HttpStatus.CREATED).body(coursEtudiantService.addCours(dto.dossierId(), dto, username));
	}

	@PreAuthorize("hasRole('ETUDIANT')")
	@DeleteMapping("{id}")
	public ResponseEntity<Void> deleteCours(@PathVariable("id") Long id,
			JwtAuthenticationToken auth) {
		String username = auth.getToken().getClaimAsString("preferred_username");
		coursEtudiantService.deleteCours(id, username);
		return ResponseEntity.noContent().build();
	}

	@PreAuthorize("hasRole('ETUDIANT')")
	@GetMapping("dossier/{dossierId}")
	public ResponseEntity<List<CoursEtudiantDto>> getCoursByDossier(@PathVariable("dossierId") Long dossierId,
			JwtAuthenticationToken auth) {
		String username = auth.getToken().getClaimAsString("preferred_username");
		return ResponseEntity.ok(coursEtudiantService.getCoursByDossier(dossierId, username));
	}
}
