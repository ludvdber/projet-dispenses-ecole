package org.isfce.pid.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.isfce.pid.exception.DossierException;
import org.isfce.pid.dao.ICorrCoursUeDao;
import org.isfce.pid.dao.ICoursEtudiantDao;
import org.isfce.pid.dao.IDispenseCoursDao;
import org.isfce.pid.dao.IDispenseDao;
import org.isfce.pid.dao.IDossierDao;
import org.isfce.pid.dao.UeDao;
import org.isfce.pid.dto.CoursEtudiantDto;
import org.isfce.pid.dto.DispenseDto;
import org.isfce.pid.model.CoursEtudiant;
import org.isfce.pid.model.Dispense;
import org.isfce.pid.model.DispenseCours;
import org.isfce.pid.model.DispenseCours.DispenseCoursId;
import org.isfce.pid.model.Dossier;
import org.isfce.pid.model.EtatDossier;
import org.isfce.pid.model.UE;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/**
 * Service métier pour les demandes de dispense.
 *
 * @author Ludovic
 */
@SuppressWarnings("null")
@Service
@RequiredArgsConstructor
@Transactional
public class DispenseService {

	private final IDossierDao daoDossier;
	private final IDispenseDao daoDispense;
	private final IDispenseCoursDao daoDispenseCours;
	private final ICoursEtudiantDao daoCoursEtudiant;
	private final ICorrCoursUeDao daoCorrCoursUe;
	private final UeDao daoUe;

	/**
	 * Crée une dispense pour une UE dans un dossier.
	 * Vérifie ownership, état du dossier et unicité (dossier, UE).
	 */
	public DispenseDto createDispense(Long dossierId, String codeUe, String username) {
		Dossier dossier = checkDossierOwnership(dossierId, username);
		checkDossierModifiable(dossier);

		UE ue = daoUe.findById(codeUe)
				.orElseThrow(() -> new DossierException("err.dispense.ueNotFound"));

		// Vérifier unicité (dossier, UE)
		if (daoDispense.findByDossierIdAndUeCode(dossierId, codeUe).isPresent()) {
			throw new DossierException("err.dispense.doublon");
		}

		Dispense dispense = new Dispense();
		dispense.setDossier(dossier);
		dispense.setUe(ue);
		dispense = daoDispense.save(dispense);

		return toDto(dispense, List.of());
	}

	/**
	 * Supprime une dispense et ses liens cours justificatifs.
	 */
	@Transactional
	public void deleteDispense(Long dispenseId, String username) {
		Dispense dispense = daoDispense.findById(dispenseId)
				.orElseThrow(() -> new DossierException("err.dispense.notFound"));

		checkDossierOwnership(dispense.getDossier().getId(), username);
		checkDossierModifiable(dispense.getDossier());

		daoDispenseCours.deleteByDispenseId(dispenseId);
		daoDispense.deleteById(dispenseId);
	}

	/**
	 * Ajoute un cours étudiant comme justificatif d'une dispense.
	 */
	public DispenseDto addCoursJustificatif(Long dispenseId, Long coursId, String username) {
		Dispense dispense = daoDispense.findById(dispenseId)
				.orElseThrow(() -> new DossierException("err.dispense.notFound"));

		checkDossierOwnership(dispense.getDossier().getId(), username);
		checkDossierModifiable(dispense.getDossier());

		CoursEtudiant cours = daoCoursEtudiant.findById(coursId)
				.orElseThrow(() -> new DossierException("err.cours.notFound"));

		// Vérifier que le cours appartient au même dossier
		if (!cours.getDossier().getId().equals(dispense.getDossier().getId())) {
			throw new DossierException("err.dispense.coursDossierMismatch");
		}

		// Vérifier pas de doublon
		DispenseCoursId dcId = new DispenseCoursId(dispenseId, coursId);
		if (daoDispenseCours.existsById(dcId)) {
			throw new DossierException("err.dispense.coursDejaLie");
		}

		DispenseCours dc = new DispenseCours(dcId, dispense, cours);
		daoDispenseCours.save(dc);

		return toDto(dispense, loadJustificatifs(dispenseId));
	}

	/**
	 * Retire un cours justificatif d'une dispense.
	 */
	public DispenseDto removeCoursJustificatif(Long dispenseId, Long coursId, String username) {
		Dispense dispense = daoDispense.findById(dispenseId)
				.orElseThrow(() -> new DossierException("err.dispense.notFound"));

		checkDossierOwnership(dispense.getDossier().getId(), username);
		checkDossierModifiable(dispense.getDossier());

		DispenseCoursId dcId = new DispenseCoursId(dispenseId, coursId);
		if (!daoDispenseCours.existsById(dcId)) {
			throw new DossierException("err.dispense.coursNonLie");
		}

		daoDispenseCours.deleteById(dcId);

		return toDto(dispense, loadJustificatifs(dispenseId));
	}

	/**
	 * Retourne toutes les dispenses d'un dossier avec leurs cours justificatifs.
	 */
	public List<DispenseDto> getDispensesByDossier(Long dossierId, String username) {
		checkDossierOwnership(dossierId, username);

		return daoDispense.findByDossierId(dossierId).stream()
				.map(d -> toDto(d, loadJustificatifs(d.getId())))
				.toList();
	}

	// --- Méthodes utilitaires ---

	private Dossier checkDossierOwnership(Long dossierId, String username) {
		Dossier dossier = daoDossier.findById(dossierId)
				.orElseThrow(() -> new DossierException("err.dossier.notFound"));
		if (!dossier.getUser().getUsername().equals(username)) {
			throw new DossierException("err.dossier.forbidden");
		}
		return dossier;
	}

	private void checkDossierModifiable(Dossier dossier) {
		if (dossier.getEtat() != EtatDossier.DEMANDE_EN_COURS
				&& dossier.getEtat() != EtatDossier.ATTENTE_COMPLEMENT) {
			throw new DossierException("err.cours.dossierClos");
		}
	}

	private List<CoursEtudiantDto> loadJustificatifs(Long dispenseId) {
		return daoDispenseCours.findByDispenseId(dispenseId).stream()
				.map(dc -> toCoursDto(dc.getCoursEtudiant()))
				.toList();
	}

	private DispenseDto toDto(Dispense d, List<CoursEtudiantDto> justificatifs) {
		boolean reconnue = !justificatifs.isEmpty()
				&& checkReconnaissance(d.getId(), d.getUe().getCode());
		return new DispenseDto(
				d.getId(),
				d.getDossier().getId(),
				d.getUe().getCode(),
				d.getUe().getNom(),
				d.getDecision(),
				justificatifs,
				reconnue);
	}

	private boolean checkReconnaissance(Long dispenseId, String ueCode) {
		List<CoursEtudiant> coursEntities = daoDispenseCours.findByDispenseId(dispenseId)
				.stream().map(DispenseCours::getCoursEtudiant).toList();

		if (!allCoursPointToUe(coursEntities, ueCode)) {
			return false;
		}
		return allCorrespondancesComplete(coursEntities, ueCode);
	}

	private boolean allCoursPointToUe(List<CoursEtudiant> cours, String ueCode) {
		return cours.stream().allMatch(c ->
				c.getCorrCours() != null &&
				c.getCorrCours().getUesISFCE().stream()
						.anyMatch(ccu -> ccu.getUe().getCode().equals(ueCode)));
	}

	private boolean allCorrespondancesComplete(List<CoursEtudiant> cours, String ueCode) {
		Set<Long> correspondanceIds = cours.stream()
				.filter(c -> c.getCorrCours() != null)
				.map(c -> c.getCorrCours().getCorrespondance().getId())
				.collect(Collectors.toSet());
		for (Long corrId : correspondanceIds) {
			long requis = daoCorrCoursUe.countByUeCodeAndCorrCoursCorrespondanceId(ueCode, corrId);
			long presents = cours.stream()
					.filter(c -> c.getCorrCours() != null
							&& c.getCorrCours().getCorrespondance().getId().equals(corrId))
					.count();
			if (presents < requis) {
				return false;
			}
		}
		return true;
	}

	private CoursEtudiantDto toCoursDto(CoursEtudiant c) {
		return new CoursEtudiantDto(
				c.getId(),
				c.getDossier().getId(),
				c.getEcole() != null ? c.getEcole().getCode() : null,
				c.getEcoleSaisie(),
				c.getCodeCours(),
				c.getIntitule(),
				c.getEcts(),
				c.getUrlFiche(),
				c.getCorrCours() != null ? c.getCorrCours().getId() : null,
				c.getStatutSaisie());
	}
}
