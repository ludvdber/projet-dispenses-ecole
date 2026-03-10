package org.isfce.pid.dto;

import java.util.List;

/**
 * DTO de suggestion : une UE ISFCE suggérée sur base des cours AUTO_RECONNU,
 * avec progression (nombre de cours présents vs requis).
 *
 * @author Ludovic
 */
public record AnalyseDto(
		String codeUe,
		String nomUe,
		int credits,
		int coursPresents,
		int coursRequis,
		boolean complet,
		List<String> coursCorrespondants) {
}
