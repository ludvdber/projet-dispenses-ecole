package org.isfce.pid.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Unité d'enseignement (UE) avec ses acquis d'apprentissage.
 *
 * @author Ludovic
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder()
@EqualsAndHashCode(exclude = "acquis")
@Entity(name = "TUE")
@Getter
public class UE {
	@Id
	@Column(length = 20)
	private String code;
	@Column(unique = true, nullable = false, length = 20)
	private String ref;
	@Column(nullable = false, length = 100)
	private String nom;
	@Column(nullable = false)
	@Min(value = 1, message = "{err.ue.nbPeriodes}")
	private int nbPeriodes;
	@Column(nullable = false)
	@Min(value = 1, message = "{err.ue.nbECTS}")
	private int ects;
	@Lob
	@Column(nullable = false)
	private String prgm;

	/** Liste des acquis — relation bidirectionnelle via {@link Acquis#ue} */
	@OneToMany(mappedBy = "ue", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
	@Builder.Default
	private List<Acquis> acquis = new ArrayList<>();

}
