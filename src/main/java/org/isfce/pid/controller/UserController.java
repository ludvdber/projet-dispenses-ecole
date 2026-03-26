package org.isfce.pid.controller;

import java.util.List;

import org.isfce.pid.dto.UserDto;

import org.isfce.pid.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Contrôleur REST pour la gestion des utilisateurs.
 * @author Ludovic
 */
@SuppressWarnings("null")
@RestController
@RequestMapping(path = "/api/user", produces = "application/json")
@RequiredArgsConstructor
@Slf4j
public class UserController {

	private final UserService userService;

	@PreAuthorize(value = "hasAnyRole('ADMIN')")
	@GetMapping(params = "all")
	public ResponseEntity<List<UserDto>> getAllUser() {
		return ResponseEntity.ok(userService.getAllUserDto());
	}

	/**
	 * Retourne le profil d'un utilisateur.
	 * Seul un ADMIN ou l'utilisateur lui-même peut consulter un profil.
	 * La vérification est faite manuellement car Eclipse JDT ne compile pas
	 * avec le flag {@code -parameters}, ce qui empêche Spring Security de
	 * résoudre les noms de paramètres dans les expressions SpEL.
	 *
	 * @author Ludovic
	 */
	@GetMapping("/profile/{username}")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<UserDto> getUserInfo(@PathVariable("username") String username,
			JwtAuthenticationToken auth) {
		// Vérifie que l'utilisateur est ADMIN ou accède à son propre profil
		boolean isAdmin = auth.getAuthorities().stream()
				.anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
		if (!isAdmin && !auth.getName().equals(username)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}

		// Lazy provisioning si l'utilisateur consulte son propre profil
		if (auth.getName().equals(username)) {
			userService.provisionFromJwt(auth);
		}
		return userService.getUserById(username)
				.map(u -> ResponseEntity.ok(new UserDto(username, u.getEmail(), u.getNom(), u.getPrenom())))
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

}
