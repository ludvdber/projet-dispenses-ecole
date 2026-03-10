package org.isfce.pid.dto;

/**
 * DTO pour les cours de la base de connaissances (CorrCours).
 *
 * @author Ludovic
 */
public record CoursDto(
		String codeCours,
		String intitule,
		Integer ects) {
}
