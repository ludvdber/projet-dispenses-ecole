package org.isfce.pid.service;

import java.util.List;

import org.isfce.pid.controller.error.DossierException;
import org.isfce.pid.dao.ICorrCoursDao;
import org.isfce.pid.dao.ICoursEtudiantDao;
import org.isfce.pid.dao.IDossierDao;
import org.isfce.pid.dao.IEcoleDao;
import org.isfce.pid.dto.CoursEtudiantDto;
import org.isfce.pid.model.CorrCours;
import org.isfce.pid.model.CoursEtudiant;
import org.isfce.pid.model.Dossier;
import org.isfce.pid.model.Ecole;
import org.isfce.pid.model.EtatDossier;
import org.isfce.pid.model.StatutSaisie;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.AllArgsConstructor;

/**
 * Service de gestion des cours externes saisis par l'étudiant.
 * @author Ludovic
 */
@SuppressWarnings("null")
@Service
@AllArgsConstructor
@Transactional
public class CoursEtudiantService {

	private IDossierDao daoDossier;
	private ICoursEtudiantDao daoCoursEtudiant;
	private DocumentService documentService;
	private IEcoleDao daoEcole;
	private ICorrCoursDao daoCorrCours;

	/**
	 * Ajoute un cours étudiant à un dossier.
	 * Vérifie ownership et état du dossier, puis auto-reconnaissance si possible.
	 */
	public CoursEtudiantDto addCours(Long dossierId, CoursEtudiantDto dto, String username) throws DossierException {
		Dossier dossier = daoDossier.findById(dossierId)
				.orElseThrow(() -> new DossierException("err.dossier.notFound"));

		if (!dossier.getUser().getUsername().equals(username)) {
			throw new DossierException("err.dossier.forbidden");
		}
		if (dossier.getEtat() != EtatDossier.DEMANDE_EN_COURS
				&& dossier.getEtat() != EtatDossier.ATTENTE_COMPLEMENT) {
			throw new DossierException("err.cours.dossierClos");
		}

		if (dto.ects() != null && dto.ects() < 1) {
			throw new DossierException("err.cours.ectsInvalide");
		}

		CoursEtudiant cours = new CoursEtudiant();
		cours.setDossier(dossier);
		cours.setCodeCours(dto.codeCours());
		cours.setIntitule(dto.intitule());
		cours.setEcts(dto.ects());
		cours.setUrlFiche(dto.urlFiche());

		// Recherche école connue
		if (dto.codeEcole() != null) {
			Ecole ecole = daoEcole.findById(dto.codeEcole()).orElse(null);
			if (ecole != null) {
				cours.setEcole(ecole);
				// Recherche correspondance cours
				CorrCours corr = daoCorrCours
						.findByCorrespondanceEcoleCodeAndCodeCours(ecole.getCode(), dto.codeCours())
						.orElse(null);
				if (corr != null) {
					cours.setCorrCours(corr);
					cours.setStatutSaisie(StatutSaisie.AUTO_RECONNU);
				} else {
					cours.setStatutSaisie(StatutSaisie.INCONNU);
				}
			} else {
				// Code école fourni mais inconnu → saisie libre
				cours.setEcoleSaisie(dto.nomEcole());
				cours.setStatutSaisie(StatutSaisie.INCONNU);
			}
		} else {
			// Pas de code école → saisie libre
			cours.setEcoleSaisie(dto.nomEcole());
			cours.setStatutSaisie(StatutSaisie.INCONNU);
		}

		cours = daoCoursEtudiant.save(cours);
		return toDto(cours);
	}

	/**
	 * Supprime un cours étudiant. Vérifie ownership via le dossier.
	 */
	public void deleteCours(Long coursId, String username) throws DossierException {
		CoursEtudiant cours = daoCoursEtudiant.findById(coursId)
				.orElseThrow(() -> new DossierException("err.cours.notFound"));

		if (!cours.getDossier().getUser().getUsername().equals(username)) {
			throw new DossierException("err.cours.forbidden");
		}
		if (cours.getDossier().getEtat() != EtatDossier.DEMANDE_EN_COURS) {
			throw new DossierException("err.cours.dossierClos");
		}

		// Supprimer fichiers physiques + enregistrements BDD des documents liés
		documentService.deleteAllByCoursEtudiantId(coursId);
		daoCoursEtudiant.deleteById(coursId);
	}

	/**
	 * Retourne les cours d'un dossier. Vérifie ownership.
	 */
	public List<CoursEtudiantDto> getCoursByDossier(Long dossierId, String username) throws DossierException {
		Dossier dossier = daoDossier.findById(dossierId)
				.orElseThrow(() -> new DossierException("err.dossier.notFound"));

		if (!dossier.getUser().getUsername().equals(username)) {
			throw new DossierException("err.dossier.forbidden");
		}

		return daoCoursEtudiant.findByDossierId(dossierId).stream()
				.map(this::toDto).toList();
	}

	private CoursEtudiantDto toDto(CoursEtudiant c) {
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
