package org.isfce.pid.dto;

import org.isfce.pid.model.StatutSaisie;

/**
 * DTO pour un cours étudiant (entrée et sortie).
 *
 * @author Ludovic
 */
public record CoursEtudiantDto(
		Long id,
		Long dossierId,
		String codeEcole,
		String nomEcole,
		String codeCours,
		String intitule,
		Integer ects,
		String urlFiche,
		Long corrCoursId,
		StatutSaisie statutSaisie) {
}
