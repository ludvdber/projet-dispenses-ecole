package org.isfce.pid.dao;

import java.util.List;

import org.isfce.pid.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * DAO pour les documents uploadés.
 *
 * @author Ludovic
 */
public interface IDocumentDao extends JpaRepository<Document, Long> {

	List<Document> findByDossierIdAndDeletedAtIsNull(Long dossierId);

	List<Document> findByCoursEtudiantIdAndDeletedAtIsNull(Long coursEtudiantId);

	int countByDossierIdAndDeletedAtIsNull(Long dossierId);
}
