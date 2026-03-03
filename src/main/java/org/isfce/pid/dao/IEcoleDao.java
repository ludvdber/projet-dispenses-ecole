package org.isfce.pid.dao;

import org.isfce.pid.model.Ecole;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * DAO pour les écoles externes (base de connaissances).
 *
 * @author Ludovic
 */
public interface IEcoleDao extends JpaRepository<Ecole, String> {
}
