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
 * Entité d'association entre un cours externe connu (CorrCours)
 * et une UE ISFCE cible.
 * Clé primaire composite (FK_CORR_COURS, FK_UE).
 *
 * @author Ludovic
 */
@Entity(name = "TCORR_COURS_UE")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@EqualsAndHashCode
public class CorrCoursUe {

	/**
	 * Clé primaire composite : cours externe + UE ISFCE.
	 */
	@Embeddable
	public static record CorrCoursUeId(
			@Column(name = "FK_CORR_COURS") Long fkCorrCours,
			@Column(name = "FK_UE") String fkUe)
	implements Serializable {
	}

	@EmbeddedId
	private CorrCoursUeId id;

	@ManyToOne
	@MapsId("fkCorrCours")
	@JoinColumn(name = "FK_CORR_COURS")
	private CorrCours corrCours;

	@ManyToOne
	@MapsId("fkUe")
	@JoinColumn(name = "FK_UE")
	private UE ue;
}
