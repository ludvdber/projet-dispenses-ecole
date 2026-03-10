package org.isfce.pid.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.isfce.pid.controller.error.DossierException;
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

import lombok.AllArgsConstructor;

/**
 * Service métier pour les demandes de dispense.
 *
 * @author Ludovic
 */
@SuppressWarnings("null")
@Service
@AllArgsConstructor
@Transactional
public class DispenseService {

	private IDossierDao daoDossier;
	private IDispenseDao daoDispense;
	private IDispenseCoursDao daoDispenseCours;
	private ICoursEtudiantDao daoCoursEtudiant;
	private ICorrCoursUeDao daoCorrCoursUe;
	private UeDao daoUe;

	/**
	 * Crée une dispense pour une UE dans un dossier.
	 * Vérifie ownership, état du dossier et unicité (dossier, UE).
	 */
	public DispenseDto createDispense(Long dossierId, String codeUe, String username) throws DossierException {
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
	public void deleteDispense(Long dispenseId, String username) throws DossierException {
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
	public DispenseDto addCoursJustificatif(Long dispenseId, Long coursId, String username) throws DossierException {
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
	public DispenseDto removeCoursJustificatif(Long dispenseId, Long coursId, String username) throws DossierException {
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
	public List<DispenseDto> getDispensesByDossier(Long dossierId, String username) throws DossierException {
		checkDossierOwnership(dossierId, username);

		return daoDispense.findByDossierId(dossierId).stream()
				.map(d -> toDto(d, loadJustificatifs(d.getId())))
				.toList();
	}

	// --- Méthodes utilitaires ---

	private Dossier checkDossierOwnership(Long dossierId, String username) throws DossierException {
		Dossier dossier = daoDossier.findById(dossierId)
				.orElseThrow(() -> new DossierException("err.dossier.notFound"));
		if (!dossier.getUser().getUsername().equals(username)) {
			throw new DossierException("err.dossier.forbidden");
		}
		return dossier;
	}

	private void checkDossierModifiable(Dossier dossier) throws DossierException {
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
		// Correspondance reconnue = chaque cours justificatif pointe vers l'UE
		// ET tous les cours requis de la correspondance sont présents
		String ueCode = d.getUe().getCode();
		boolean reconnue = !justificatifs.isEmpty();
		if (reconnue) {
			List<CoursEtudiant> coursEntities = daoDispenseCours.findByDispenseId(d.getId())
					.stream().map(DispenseCours::getCoursEtudiant).toList();
			// Vérifier que chaque cours lié a un CorrCours pointant vers cette UE
			reconnue = coursEntities.stream().allMatch(c ->
					c.getCorrCours() != null &&
					c.getCorrCours().getUesISFCE().stream()
							.anyMatch(ccu -> ccu.getUe().getCode().equals(ueCode)));
			// Vérifier la complétude : tous les cours requis de la correspondance sont présents
			if (reconnue) {
				Set<Long> correspondanceIds = coursEntities.stream()
						.filter(c -> c.getCorrCours() != null)
						.map(c -> c.getCorrCours().getCorrespondance().getId())
						.collect(Collectors.toSet());
				for (Long corrId : correspondanceIds) {
					long requis = daoCorrCoursUe.countByUeCodeAndCorrCoursCorrespondanceId(ueCode, corrId);
					long presents = coursEntities.stream()
							.filter(c -> c.getCorrCours() != null
									&& c.getCorrCours().getCorrespondance().getId().equals(corrId))
							.count();
					if (presents < requis) {
						reconnue = false;
						break;
					}
				}
			}
		}
		return new DispenseDto(
				d.getId(),
				d.getDossier().getId(),
				ueCode,
				d.getUe().getNom(),
				d.getDecision(),
				justificatifs,
				reconnue);
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
