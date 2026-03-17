package org.isfce.pid.service;

import java.util.List;
import java.util.Optional;

import org.isfce.pid.dao.IUserDao;
import org.isfce.pid.dto.UserDto;
import org.isfce.pid.model.User;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

/**
 * Service de gestion des utilisateurs.
 * Gère le lazy provisioning depuis Keycloak.
 * @author Ludovic
 */
@SuppressWarnings("null")
@Transactional
@Service
@Slf4j
public class UserService {
	IUserDao daoUser;

	public UserService(IUserDao daoUser) {
		this.daoUser = daoUser;
	}

	public List<UserDto> getAllUserDto() {
		return daoUser.getAllUserDto();
	}

	public Optional<User> getUserById(String username) {
		return daoUser.findById(username);
	}

	public User addUser(User user) {
		return daoUser.save(user);
	}

	public boolean existByUsername(String username) {
		return daoUser.existsById(username);
	}

	/**
	 * Crée le user en BDD à partir des claims JWT Keycloak
	 * s'il n'existe pas encore (lazy provisioning).
	 * @return le User existant ou nouvellement créé
	 */
	public User provisionFromJwt(JwtAuthenticationToken auth) {
		String username = auth.getToken().getClaimAsString("preferred_username");
		return daoUser.findById(username).orElseGet(() -> {
			var token = auth.getToken();
			String email = token.getClaimAsString("email");
			String nom = token.getClaimAsString("family_name");
			String prenom = token.getClaimAsString("given_name");
			log.info("Provisionnement automatique du user '{}'", username);
			return daoUser.save(new User(username, email, nom, prenom));
		});
	}

}
