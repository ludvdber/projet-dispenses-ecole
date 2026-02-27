package org.isfce.pid.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Entité représentant une école externe (base de connaissances).
 *
 * @author Ludovic
 */
@Entity(name = "TECOLE")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class Ecole {

	@EqualsAndHashCode.Include
	@Id
	@Column(name = "CODE", length = 20)
	private String code;

	@Column(name = "NOM", length = 200, nullable = false)
	private String nom;

	@Column(name = "URL_SITE", length = 500, nullable = true)
	private String urlSite;
}
