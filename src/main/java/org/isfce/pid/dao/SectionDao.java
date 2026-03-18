package org.isfce.pid.dao;

import org.isfce.pid.model.Section;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * DAO Spring Data JPA pour l'entité Section.
 * @author Ludovic
 */
public interface SectionDao extends JpaRepository<Section, String> {
}
