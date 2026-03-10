package org.isfce.pid.dto;

import java.time.LocalDateTime;

import org.isfce.pid.model.TypeDoc;

/**
 * DTO pour un document uploadé.
 *
 * @author Ludovic
 */
public record DocumentDto(
		Long id,
		Long dossierId,
		Long coursEtudiantId,
		TypeDoc typeDoc,
		String originalFilename,
		String typeMime,
		Long taille,
		LocalDateTime dateDepot,
		String hashSha256) {
}
