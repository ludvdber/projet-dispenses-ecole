package org.isfce.pid.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

/**
 * DTO complet d'une UE avec ses acquis.
 * @author Ludovic
 */
public record UEFullDto(
	@NotBlank String code,
	@NotBlank String ref,
	@NotBlank String nom,
	@Min(value = 1, message = "{err.ue.nbPeriodes}") int nbPeriodes,
	@Min(value = 1, message = "{err.ue.nbECTS}") int ects,
	@NotBlank String prgm,
	@Size(min = 1, message = "{err.ue.acquis}") @NotEmpty @Valid List<AcquisFullDto> acquis
) {}
