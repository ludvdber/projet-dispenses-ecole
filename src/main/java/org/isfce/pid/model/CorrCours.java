package org.isfce.pid.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
 * Entité représentant un cours d'une école externe faisant partie
 * d'une correspondance validée.
 *
 * @author Ludovic
 */
@Entity(name = "TCORR_COURS")
@Table(name = "TCORR_COURS",
		uniqueConstraints = @UniqueConstraint(columnNames = { "FK_CORRESPONDANCE", "CODE_COURS" }))
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CorrCours {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "FK_CORRESPONDANCE", nullable = false)
	private Correspondance correspondance;

	@Column(name = "CODE_COURS", length = 50, nullable = false)
	private String codeCours;

	@Column(name = "INTITULE", length = 200, nullable = false)
	private String intitule;

	@Column(name = "ECTS", nullable = true)
	private Integer ects;

	@Column(name = "URL_FICHE_OFFIC", length = 500, nullable = true)
	private String urlFicheOffic;
}
