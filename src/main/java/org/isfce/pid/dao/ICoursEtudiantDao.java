package org.isfce.pid.dao;

import java.util.List;

import org.isfce.pid.model.CoursEtudiant;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * DAO pour les cours suivis par un étudiant dans une école externe.
 *
 * @author Ludovic
 */
public interface ICoursEtudiantDao extends JpaRepository<CoursEtudiant, Long> {

	List<CoursEtudiant> findByDossierId(Long dossierId);

	int countByDossierId(Long dossierId);
}
