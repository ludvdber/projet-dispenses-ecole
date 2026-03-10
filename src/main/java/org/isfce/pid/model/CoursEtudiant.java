package org.isfce.pid.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entité représentant un cours suivi par un étudiant dans une école externe,
 * rattaché à un dossier de dispense.
 *
 * @author Ludovic
 */
@Entity(name = "TCOURS_ETUDIANT")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CoursEtudiant {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "FK_DOSSIER", nullable = false)
	private Dossier dossier;

	/** École connue (FK). XOR avec ecoleSaisie. */
	@ManyToOne
	@JoinColumn(name = "FK_ECOLE", nullable = true)
	private Ecole ecole;

	/** Nom d'école saisi librement. XOR avec ecole. */
	@Column(name = "ECOLE_SAISIE", length = 200, nullable = true)
	private String ecoleSaisie;

	@Column(name = "CODE_COURS", length = 50, nullable = false)
	private String codeCours;

	@Column(name = "INTITULE", length = 200, nullable = false)
	private String intitule;

	@Min(value = 1, message = "Les crédits ECTS doivent être au moins 1")
	@Column(name = "ECTS", nullable = true)
	private Integer ects;

	/** Correspondance cours connue, null si cours inconnu. */
	@ManyToOne
	@JoinColumn(name = "FK_CORR_COURS", nullable = true)
	private CorrCours corrCours;

	@Column(name = "URL_FICHE", length = 500, nullable = true)
	private String urlFiche;

	@Enumerated(EnumType.STRING)
	@Column(name = "STATUT_SAISIE", length = 20, nullable = false)
	private StatutSaisie statutSaisie;

	/**
	 * Validation XOR : l'école doit être renseignée d'une seule manière (FK ou saisie libre).
	 */
	@AssertTrue(message = "L'école doit être renseignée soit par une référence connue, soit par saisie libre, mais pas les deux ni aucune")
	public boolean isEcoleXorValid() {
		boolean aEcole = (ecole != null);
		boolean aSaisie = (ecoleSaisie != null && !ecoleSaisie.isBlank());
		return aEcole ^ aSaisie;
	}
}
