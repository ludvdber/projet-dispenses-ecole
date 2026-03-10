package org.isfce.pid.dto;

/**
 * DTO d'un utilisateur (username, email, nom, prenom).
 * @author Ludovic
 */
public record UserDto(String username, String email, String nom, String prenom) {

}
