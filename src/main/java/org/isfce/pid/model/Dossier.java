	package org.isfce.pid.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Dossier de demande de dispense d'un étudiant.
 *
 * @author Ludovic
 */
@Entity(name = "TDOSSIER")
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@AllArgsConstructor
@Getter
public class Dossier {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "FK_USER", nullable = false)
	private final User user;

	@Column(name = "DATE_CREATION", nullable = false)
	private final LocalDate dateCreation;

	@Column(name = "DATE_SOUMIS", nullable = true)
	@Setter
	private LocalDate dateSoumis;

	@Lob
	@Column(name = "OBJET_DEMANDE", nullable = true)
	@Setter
	private String objetDemande;

	@Setter
	@Column(nullable = false, length = 30)
	@Enumerated(EnumType.STRING)
	private EtatDossier etat;

	@Builder.Default
	@Column(name = "COMPLET", nullable = false)
	@Setter
	private boolean complet = false;
}
