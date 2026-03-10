package org.isfce.pid.dto;

/**
 * Résultat de la vérification de complétude d'un dossier.
 *
 * @author Ludovic
 */
public record CompletudeDossier(
		boolean complet,
		boolean bulletinOk,
		boolean motivationOk,
		boolean dispensesOk,
		boolean coursInconnusOk,
		boolean hasCoursInconnus) {
}
