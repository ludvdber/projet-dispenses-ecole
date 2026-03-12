package org.isfce.pid.dto;

import org.isfce.pid.model.StatutSaisie;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO pour un cours étudiant (entrée et sortie).
 *
 * @author Ludovic
 */
public record CoursEtudiantDto(
		Long id,
		Long dossierId,
		String codeEcole,
		@Size(max = 200) String nomEcole,
		@NotBlank @Size(max = 100) String codeCours,
		@NotBlank @Size(max = 100) String intitule,
		@Min(0) @Max(300) Integer ects,
		@Size(max = 200) String urlFiche,
		Long corrCoursId,
		StatutSaisie statutSaisie) {
}
