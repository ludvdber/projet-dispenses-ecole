package org.isfce.pid.dao;

import java.util.List;
import java.util.Optional;

import org.isfce.pid.model.CorrCours;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * DAO pour les cours externes d'une correspondance.
 *
 * @author Ludovic
 */
public interface ICorrCoursDao extends JpaRepository<CorrCours, Long> {

	List<CorrCours> findByCorrespondanceId(Long correspondanceId);

	Optional<CorrCours> findByCorrespondanceEcoleCodeAndCodeCours(String ecoleCode, String codeCours);

	List<CorrCours> findByCorrespondanceEcoleCode(String ecoleCode);
}
