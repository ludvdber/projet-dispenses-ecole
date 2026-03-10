package org.isfce.pid.dao;

import java.util.List;

import org.isfce.pid.model.CoursEtudiant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * DAO pour les cours suivis par un étudiant dans une école externe.
 *
 * @author Ludovic
 */
public interface ICoursEtudiantDao extends JpaRepository<CoursEtudiant, Long> {

	List<CoursEtudiant> findByDossierId(Long dossierId);

	/**
	 * Charge les cours d'un dossier avec toute la chaîne CorrCours → Correspondance
	 * pour éviter les N+1 dans AnalyseService.
	 */
	@Query("SELECT DISTINCT c FROM TCOURS_ETUDIANT c "
			+ "LEFT JOIN FETCH c.corrCours cc "
			+ "LEFT JOIN FETCH cc.correspondance "
			+ "LEFT JOIN FETCH cc.uesISFCE ccu "
			+ "LEFT JOIN FETCH ccu.ue "
			+ "WHERE c.dossier.id = :dossierId")
	List<CoursEtudiant> findByDossierIdWithCorrespondances(Long dossierId);

	int countByDossierId(Long dossierId);
}
