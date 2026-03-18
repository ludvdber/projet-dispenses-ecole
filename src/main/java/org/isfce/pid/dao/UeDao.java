package org.isfce.pid.dao;

import org.isfce.pid.model.UE;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * DAO Spring Data JPA pour l'entité UE.
 * @author Ludovic
 */
public interface UeDao extends JpaRepository<UE, String> {
}
