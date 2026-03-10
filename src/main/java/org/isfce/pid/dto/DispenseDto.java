package org.isfce.pid.dto;

import java.util.List;

import org.isfce.pid.model.DecisionDispense;

/**
 * DTO pour une demande de dispense.
 *
 * @author Ludovic
 */
public record DispenseDto(
		Long id,
		Long dossierId,
		String codeUe,
		String nomUe,
		DecisionDispense decision,
		List<CoursEtudiantDto> coursJustificatifs,
		boolean correspondanceReconnue) {
}
