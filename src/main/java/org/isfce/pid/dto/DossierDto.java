package org.isfce.pid.dto;

import java.time.LocalDate;

import org.isfce.pid.model.EtatDossier;

/**
 * DTO pour un dossier de dispense.
 *
 * @author Ludovic
 */
public record DossierDto(Long id, LocalDate dateCreation, LocalDate dateSoumis,
		EtatDossier etat, String objetDemande, boolean complet, UserDto user) {

}
