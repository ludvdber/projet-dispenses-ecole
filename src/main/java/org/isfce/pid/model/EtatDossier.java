package org.isfce.pid.model;

/**
 * États possibles d'un dossier de dispense.
 *
 * @author Ludovic
 */
public enum EtatDossier {
	DEMANDE_EN_COURS,
	TRAITEMENT_DIRECTION,
	TRAITEMENT_ENSEIGNANT,
	ATTENTE_COMPLEMENT,
	CLOTURE_ACCORDE,
	CLOTURE_REFUSE
}
