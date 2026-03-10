package org.isfce.pid.model;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Acquis d'apprentissage rattaché à une UE.
 * Clé primaire composée (FKUE, NUM) via {@link IdAcquis}.
 *
 * @author Ludovic
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Entity(name = "TACQUIS")
public class Acquis {

	/**
	 * Clé primaire composée : code UE + numéro séquentiel.
	 */
	@Embeddable
	public static record IdAcquis(
			@Column(name = "FKUE") String fkUE,
			Integer num)
	implements Serializable {}

	@EmbeddedId
	private IdAcquis id;

	/** Relation vers l'UE — réutilise la colonne FKUE de l'@EmbeddedId */
	@ManyToOne
	@MapsId("fkUE")
	@JoinColumn(name = "FKUE")
	private UE ue;

	@NotBlank
	@Column(length = 500, nullable = false)
	private String acquis;

	@Min(value = 1, message = "{err.acquis.pourcentage.min}")
	@Max(value = 100, message = "{err.acquis.pourcentage.max}")
	@Column(nullable = false)
	private Integer pourcentage;
}
