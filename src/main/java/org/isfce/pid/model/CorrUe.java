package org.isfce.pid.model;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Entité d'association entre une correspondance et une UE ISFCE cible.
 * Clé primaire composite (FK_CORRESPONDANCE, FK_UE).
 *
 * @author Ludovic
 */
@Entity(name = "TCORR_UE")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@EqualsAndHashCode
public class CorrUe {

	/**
	 * Clé primaire composite : correspondance + UE.
	 */
	@Embeddable
	public static record CorrUeId(
			@Column(name = "FK_CORRESPONDANCE") Long fkCorrespondance,
			@Column(name = "FK_UE") String fkUe)
	implements Serializable {
	}

	@EmbeddedId
	private CorrUeId id;

	@ManyToOne
	@MapsId("fkCorrespondance")
	@JoinColumn(name = "FK_CORRESPONDANCE")
	private Correspondance correspondance;

	@ManyToOne
	@MapsId("fkUe")
	@JoinColumn(name = "FK_UE")
	private UE ue;
}
