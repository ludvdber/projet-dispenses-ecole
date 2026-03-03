package org.isfce.pid.dao;

import java.util.List;
import java.util.Optional;

import org.isfce.pid.model.Dispense;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * DAO pour les dispenses.
 *
 * @author Ludovic
 */
public interface IDispenseDao extends JpaRepository<Dispense, Long> {

	List<Dispense> findByDossierId(Long dossierId);

	Optional<Dispense> findByDossierIdAndUeCode(Long dossierId, String ueCode);

	int countByDossierId(Long dossierId);
}
