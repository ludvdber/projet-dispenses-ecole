package org.isfce.pid.model;

import java.time.LocalDateTime;

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
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entité représentant un document uploadé (bulletin, programme de cours, etc.).
 *
 * @author Ludovic
 */
@Entity(name = "TDOCUMENT")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Document {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** Dossier parent. XOR avec coursEtudiant. */
	@ManyToOne
	@JoinColumn(name = "FK_DOSSIER", nullable = true)
	private Dossier dossier;

	/** Cours étudiant parent. XOR avec dossier. */
	@ManyToOne
	@JoinColumn(name = "FK_COURS_ETUDIANT", nullable = true)
	private CoursEtudiant coursEtudiant;

	@ManyToOne
	@JoinColumn(name = "FK_ECOLE_DOC", nullable = true)
	private Ecole ecoleDoc;

	@Column(name = "ECOLE_SAISIE_DOC", length = 200, nullable = true)
	private String ecoleSaisieDoc;

	@Enumerated(EnumType.STRING)
	@Column(name = "TYPE_DOC", length = 20, nullable = false)
	private TypeDoc typeDoc;

	@Column(name = "ORIGINAL_FILENAME", length = 255, nullable = false)
	private String originalFilename;

	@Column(name = "CHEMIN_RELATIF", length = 500, nullable = false)
	private String cheminRelatif;

	@Column(name = "TYPE_MIME", length = 100, nullable = false)
	private String typeMime;

	@Column(name = "TAILLE", nullable = false)
	private Long taille;

	@Column(name = "DATE_DEPOT", nullable = false)
	private LocalDateTime dateDepot;

	/** null = actif, non-null = supprimé (soft delete). */
	@Column(name = "DELETED_AT", nullable = true)
	private LocalDateTime deletedAt;

	/** Hash SHA-256 calculé après upload. */
	@Column(name = "HASH_SHA256", length = 64, nullable = true)
	private String hashSha256;

	/**
	 * Validation XOR : un document doit être lié soit à un dossier soit à un
	 * cours étudiant, pas les deux.
	 */
	@AssertTrue(message = "Un document doit être lié soit à un dossier soit à un cours étudiant, pas les deux")
	public boolean isDossierXorCoursEtudiantValid() {
		return (dossier == null) ^ (coursEtudiant == null);
	}
}
