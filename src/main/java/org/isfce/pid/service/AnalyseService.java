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

import lombok.AllArgsConstructor;

/**
 * Service d'analyse : suggestions d'UE ISFCE basées sur les cours AUTO_RECONNU.
 * Vérifie la complétude par correspondance (tous les cours requis d'une même
 * correspondance doivent être présents pour que la suggestion soit "complète").
 *
 * @author Ludovic
 */
@SuppressWarnings("null")
@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class AnalyseService {

	private IDossierDao daoDossier;
	private ICoursEtudiantDao daoCoursEtudiant;
	private IDispenseDao daoDispense;
	private ICorrCoursUeDao daoCorrCoursUe;

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
	public List<AnalyseDto> analyserDossier(Long dossierId, String username) {
		Dossier dossier = daoDossier.findById(dossierId)
				.orElseThrow(() -> new DossierException("err.dossier.notFound"));
		if (!dossier.getUser().getUsername().equals(username)) {
			throw new DossierException("err.dossier.forbidden");
		}

		// UE déjà couvertes par des dispenses existantes
		Set<String> ueDejaDispensees = daoDispense.findByDossierId(dossierId).stream()
				.map(d -> d.getUe().getCode())
				.collect(Collectors.toSet());

		// Cours AUTO_RECONNU du dossier (JOIN FETCH pour éviter N+1)
		List<CoursEtudiant> coursAutoReconnus = daoCoursEtudiant.findByDossierIdWithCorrespondances(dossierId).stream()
				.filter(c -> c.getStatutSaisie() == StatutSaisie.AUTO_RECONNU && c.getCorrCours() != null)
				.toList();

		// Clé = (correspondanceId, ueCode) → nombre de cours présents
		// On regroupe les cours par correspondance et par UE cible
		record CorrUeKey(Long correspondanceId, String ueCode) {}

		Map<CorrUeKey, Integer> coursPresentsMap = new HashMap<>();
		Map<CorrUeKey, String> nomUeMap = new HashMap<>();
		Map<CorrUeKey, Integer> ectsMap = new HashMap<>();
		Map<CorrUeKey, List<String>> coursNomsMap = new HashMap<>();

		for (CoursEtudiant cours : coursAutoReconnus) {
			Long corrId = cours.getCorrCours().getCorrespondance().getId();
			String label = cours.getCorrCours().getCodeCours() + " — " + cours.getCorrCours().getIntitule();
			for (CorrCoursUe ccu : cours.getCorrCours().getUesISFCE()) {
				String ueCode = ccu.getUe().getCode();
				CorrUeKey key = new CorrUeKey(corrId, ueCode);
				coursPresentsMap.merge(key, 1, Integer::sum);
				nomUeMap.putIfAbsent(key, ccu.getUe().getNom());
				ectsMap.putIfAbsent(key, ccu.getUe().getEcts());
				coursNomsMap.computeIfAbsent(key, k -> new ArrayList<>()).add(label);
			}
		}

		// Pour chaque (correspondance, UE), compter le total requis et construire le DTO
		// Si plusieurs correspondances couvrent la même UE, garder la meilleure
		Map<String, AnalyseDto> bestPerUe = new HashMap<>();

		for (var entry : coursPresentsMap.entrySet()) {
			CorrUeKey key = entry.getKey();
			String ueCode = key.ueCode();

			if (ueDejaDispensees.contains(ueCode)) continue;

			int presents = entry.getValue();
			long requis = daoCorrCoursUe.countByUeCodeAndCorrCoursCorrespondanceId(ueCode, key.correspondanceId());

			AnalyseDto dto = new AnalyseDto(
					ueCode,
					nomUeMap.get(key),
					ectsMap.get(key),
					presents,
					(int) requis,
					presents >= requis,
					coursNomsMap.getOrDefault(key, List.of()));

			// Garder la meilleure correspondance pour cette UE
			AnalyseDto existing = bestPerUe.get(ueCode);
			if (existing == null || isBetter(dto, existing)) {
				bestPerUe.put(ueCode, dto);
			}
		}

		return new ArrayList<>(bestPerUe.values());
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
