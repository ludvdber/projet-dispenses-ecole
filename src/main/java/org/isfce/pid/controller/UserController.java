package org.isfce.pid.controller;

import java.util.List;

import org.isfce.pid.dto.UserDto;
import org.isfce.pid.model.User;
import org.isfce.pid.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping(path = "/api/user", produces = "application/json")
@CrossOrigin("*")
@Slf4j
public class UserController {

	UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	@PreAuthorize(value = "hasAnyRole('ADMIN')")
	@GetMapping(params = "all")
	public ResponseEntity<List<UserDto>> getAllUser() {
		return ResponseEntity.ok(userService.getAllUserDto());
	}

	/**
	 * Retourne le profil d'un utilisateur.
	 * Le paramètre est nommé "user" pour correspondre à l'expression SpEL
	 * {@code #user == authentication.name} dans {@code @PreAuthorize}.
	 * Le flag de compilation {@code -parameters} (configuré dans build.gradle)
	 * permet à Spring Security de résoudre le nom du paramètre par réflexion,
	 * sans nécessiter d'annotation {@code @Param}.
	 *
	 * @author Ludovic
	 */
	@GetMapping("/profile/{id}")
	@PreAuthorize(value = "hasRole('ADMIN') or #user == authentication.name")
	public ResponseEntity<UserDto> getUserInfo(@PathVariable("id") String user,
			JwtAuthenticationToken auth) {
		var oUser = userService.getUserById(user);
		log.info(auth.getName() + "  username: " + user);
		User userEntity = oUser.orElse(null);

		// Crée l'utilisateur s'il n'existe pas et que
		// l'utilisateur connecté correspond au username demandé
		if (oUser.isEmpty() && auth.getName().equals(user)) {
			var token = auth.getToken();
			String email  = token.getClaimAsString("email");
			String nom    = token.getClaimAsString("family_name");
			String prenom = token.getClaimAsString("given_name");
			userEntity = userService.addUser(new User(user, email, nom, prenom));
		}
		if (userEntity != null)
			return ResponseEntity.ok(new UserDto(user, userEntity.getEmail(), userEntity.getNom(), userEntity.getPrenom()));
		return ResponseEntity.notFound().build();
	}

}
