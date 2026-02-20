package org.isfce.pid.dto;

import java.time.LocalDate;

import org.isfce.pid.model.EtatDossier;

import jakarta.validation.constraints.NotBlank;

public record DossierDto(Long id, LocalDate date, EtatDossier etat, @NotBlank String objetDemande, UserDto user) {

}
