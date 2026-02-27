package org.isfce.pid.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Utilisateur de l'application (étudiant ou enseignant).
 * Données synchronisées depuis Keycloak.
 *
 * @author Ludovic
 */
@Entity(name = "TUSER")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
@Setter
public class User {
	@EqualsAndHashCode.Include
	@Id
	@Column(length = 50, nullable = false)
	private String username;
	@Column(length = 50, nullable = false)
	@Email
	private String email;
	@Column(length = 30, nullable = false)
	private String nom;
	@Column(length = 30, nullable = true)
	private String prenom;

}
