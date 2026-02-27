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
 * Entité d'association entre une dispense et les cours étudiants
 * qui la justifient.
 * Clé primaire composite (FK_DISPENSE, FK_COURS_ETUDIANT).
 *
 * @author Ludovic
 */
@Entity(name = "TDISPENSE_COURS")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@EqualsAndHashCode
public class DispenseCours {

	/**
	 * Clé primaire composite : dispense + cours étudiant.
	 */
	@Embeddable
	public static record DispenseCoursId(
			@Column(name = "FK_DISPENSE") Long fkDispense,
			@Column(name = "FK_COURS_ETUDIANT") Long fkCoursEtudiant)
	implements Serializable {
	}

	@EmbeddedId
	private DispenseCoursId id;

	@ManyToOne
	@MapsId("fkDispense")
	@JoinColumn(name = "FK_DISPENSE")
	private Dispense dispense;

	@ManyToOne
	@MapsId("fkCoursEtudiant")
	@JoinColumn(name = "FK_COURS_ETUDIANT")
	private CoursEtudiant coursEtudiant;
}
