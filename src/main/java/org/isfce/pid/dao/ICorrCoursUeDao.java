package org.isfce.pid.dao;

import org.isfce.pid.model.CorrCoursUe;
import org.isfce.pid.model.CorrCoursUe.CorrCoursUeId;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * DAO pour l'association cours externe → UE ISFCE.
 *
 * @author Ludovic
 */
public interface ICorrCoursUeDao extends JpaRepository<CorrCoursUe, CorrCoursUeId> {

	/**
	 * Compte le nombre de cours externes d'une correspondance donnée
	 * qui sont mappés vers une UE ISFCE donnée.
	 * Sert à déterminer combien de cours sont requis pour justifier la dispense.
	 */
	long countByUeCodeAndCorrCoursCorrespondanceId(String ueCode, Long correspondanceId);
}
