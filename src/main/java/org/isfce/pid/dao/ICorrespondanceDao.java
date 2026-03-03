package org.isfce.pid.dao;

import java.util.List;

import org.isfce.pid.model.Correspondance;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * DAO pour les correspondances validées.
 *
 * @author Ludovic
 */
public interface ICorrespondanceDao extends JpaRepository<Correspondance, Long> {

	List<Correspondance> findByEcoleCode(String codeEcole);
}
