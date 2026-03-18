package org.isfce.pid.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO d'un acquis sans référence à l'UE (utilisé dans UEFullDto).
 * @author Ludovic
 */
public record AcquisFullDto(
	@NotNull Integer num,
	@NotBlank String acquis,
	@Min(value = 1, message = "{err.acquis.pourcentage.min}")
	@Max(value = 100, message = "{err.acquis.pourcentage.max}")
	Integer pourcentage
) {}
