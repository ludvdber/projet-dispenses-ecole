package org.isfce.pid.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entité représentant une correspondance validée entre une école externe et
 * des UEs ISFCE.
 *
 * @author Ludovic
 */
@Entity(name = "TCORRESPONDANCE")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Correspondance {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "FK_ECOLE", nullable = false)
	private Ecole ecole;

	@Column(name = "DATE_VALIDATION", nullable = true)
	private LocalDate dateValidation;

	@Column(name = "NOTES", length = 500, nullable = true)
	private String notes;

	@Column(name = "ECTS_MIN_EXTERNE", nullable = true)
	private Integer ectsMinExterne;
}
