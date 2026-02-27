package org.isfce.pid.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entité représentant une demande de dispense pour une UE ISFCE,
 * rattachée à un dossier.
 *
 * @author Ludovic
 */
@Entity(name = "TDISPENSE")
@Table(name = "TDISPENSE",
		uniqueConstraints = @UniqueConstraint(columnNames = { "FK_DOSSIER", "FK_UE" }))
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Dispense {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "FK_DOSSIER", nullable = false)
	private Dossier dossier;

	@ManyToOne
	@JoinColumn(name = "FK_UE", nullable = false)
	private UE ue;

	/** null = en attente de décision. */
	@Enumerated(EnumType.STRING)
	@Column(name = "DECISION", length = 10, nullable = true)
	private DecisionDispense decision;

	@Column(name = "NOTE", precision = 4, scale = 2, nullable = true)
	private BigDecimal note;

	@Column(name = "DATE_DECISION", nullable = true)
	private LocalDate dateDecision;

	@ManyToOne
	@JoinColumn(name = "FK_VALIDATEUR", nullable = true)
	private User validateur;

	@Column(name = "COMMENTAIRE", length = 500, nullable = true)
	private String commentaire;
}
