package org.isfce.pid.dao;

import java.util.Collection;
import java.util.List;

import org.isfce.pid.model.DispenseCours;
import org.isfce.pid.model.DispenseCours.DispenseCoursId;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * DAO pour la table pivot TDISPENSE_COURS.
 *
 * @author Ludovic
 */
public interface IDispenseCoursDao extends JpaRepository<DispenseCours, DispenseCoursId> {

	List<DispenseCours> findByDispenseId(Long dispenseId);

	List<DispenseCours> findByDispenseIdIn(Collection<Long> dispenseIds);

	void deleteByDispenseId(Long dispenseId);
}
