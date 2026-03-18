package org.isfce.pid.dto;

/**
 * DTO d'une UE sans les acquis.
 * @author Ludovic
 */
public record UEDto(
	String code,
	String ref,
	String nom,
	int nbPeriodes,
	int ects,
	String prgm
) {}
