package org.isfce.pid.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.isfce.pid.exception.DossierException;
import org.isfce.pid.dao.ICorrCoursUeDao;
import org.isfce.pid.dao.ICoursEtudiantDao;
import org.isfce.pid.dao.IDispenseDao;
import org.isfce.pid.dao.IDossierDao;
import org.isfce.pid.dto.AnalyseDto;
import org.isfce.pid.model.CoursEtudiant;
import org.isfce.pid.model.CorrCoursUe;
import org.isfce.pid.model.Dossier;
import org.isfce.pid.model.StatutSaisie;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/**
 * Service d'analyse : suggestions d'UE ISFCE basées sur les cours AUTO_RECONNU.
 * Vérifie la complétude par correspondance (tous les cours requis d'une même
 * correspondance doivent être présents pour que la suggestion soit "complète").
 *
 * @author Ludovic
 */
@SuppressWarnings("null")
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyseService {

	private final IDossierDao daoDossier;
	private final ICoursEtudiantDao daoCoursEtudiant;
	private final IDispenseDao daoDispense;
	private final ICorrCoursUeDao daoCorrCoursUe;

	/**
	 * Analyse un dossier et retourne les suggestions d'UE ISFCE.
	 *
	 * Pour chaque UE découverte via les cours AUTO_RECONNU :
	 * - Compte combien de cours de la même correspondance pointent vers cette UE (requis)
	 * - Compte combien l'étudiant a déjà ajoutés (présents)
	 * - Indique si la correspondance est complète
	 *
	 * Si plusieurs correspondances couvrent la même UE, la meilleure est retenue.
	 */
	record CorrUeKey(Long correspondanceId, String ueCode) {}

	public List<AnalyseDto> analyserDossier(Long dossierId, String username) {
		Dossier dossier = daoDossier.findById(dossierId)
				.orElseThrow(() -> new DossierException("err.dossier.notFound"));
		if (!dossier.getUser().getUsername().equals(username)) {
			throw new DossierException("err.dossier.forbidden");
		}

		Set<String> ueDejaDispensees = daoDispense.findByDossierId(dossierId).stream()
				.map(d -> d.getUe().getCode())
				.collect(Collectors.toSet());

		List<CoursEtudiant> coursAutoReconnus = daoCoursEtudiant.findByDossierIdWithCorrespondances(dossierId).stream()
				.filter(c -> c.getStatutSaisie() == StatutSaisie.AUTO_RECONNU && c.getCorrCours() != null)
				.toList();

		Map<CorrUeKey, List<CoursEtudiant>> groupedCours = groupByCorrespondanceAndUe(coursAutoReconnus);
		return selectBestPerUe(groupedCours, ueDejaDispensees);
	}

	private Map<CorrUeKey, List<CoursEtudiant>> groupByCorrespondanceAndUe(List<CoursEtudiant> cours) {
		Map<CorrUeKey, List<CoursEtudiant>> result = new HashMap<>();
		for (CoursEtudiant c : cours) {
			Long corrId = c.getCorrCours().getCorrespondance().getId();
			for (CorrCoursUe ccu : c.getCorrCours().getUesISFCE()) {
				CorrUeKey key = new CorrUeKey(corrId, ccu.getUe().getCode());
				result.computeIfAbsent(key, k -> new ArrayList<>()).add(c);
			}
		}
		return result;
	}

	private List<AnalyseDto> selectBestPerUe(Map<CorrUeKey, List<CoursEtudiant>> groupedCours,
			Set<String> ueDejaDispensees) {
		Map<String, AnalyseDto> bestPerUe = new HashMap<>();

		for (var entry : groupedCours.entrySet()) {
			CorrUeKey key = entry.getKey();
			String ueCode = key.ueCode();
			if (ueDejaDispensees.contains(ueCode)) continue;

			AnalyseDto dto = buildAnalyseDto(key, entry.getValue());

			AnalyseDto existing = bestPerUe.get(ueCode);
			if (existing == null || isBetter(dto, existing)) {
				bestPerUe.put(ueCode, dto);
			}
		}
		return new ArrayList<>(bestPerUe.values());
	}

	private AnalyseDto buildAnalyseDto(CorrUeKey key, List<CoursEtudiant> cours) {
		int presents = cours.size();
		long requis = daoCorrCoursUe.countByUeCodeAndCorrCoursCorrespondanceId(key.ueCode(), key.correspondanceId());

		String nomUe = null;
		int ects = 0;
		List<String> coursNoms = new ArrayList<>();
		for (CoursEtudiant c : cours) {
			coursNoms.add(c.getCorrCours().getCodeCours() + " — " + c.getCorrCours().getIntitule());
			for (CorrCoursUe ccu : c.getCorrCours().getUesISFCE()) {
				if (ccu.getUe().getCode().equals(key.ueCode())) {
					nomUe = ccu.getUe().getNom();
					ects = ccu.getUe().getEcts();
					break;
				}
			}
		}

		return new AnalyseDto(key.ueCode(), nomUe, ects, presents, (int) requis,
				presents >= requis, coursNoms);
	}

	/**
	 * Compare deux suggestions pour la même UE.
	 * Préfère la complète, sinon celle avec le meilleur ratio.
	 */
	private boolean isBetter(AnalyseDto candidate, AnalyseDto existing) {
		if (candidate.complet() && !existing.complet()) return true;
		if (!candidate.complet() && existing.complet()) return false;
		// Même statut → meilleur ratio
		double ratioCandidate = (double) candidate.coursPresents() / candidate.coursRequis();
		double ratioExisting = (double) existing.coursPresents() / existing.coursRequis();
		return ratioCandidate > ratioExisting;
	}
}
