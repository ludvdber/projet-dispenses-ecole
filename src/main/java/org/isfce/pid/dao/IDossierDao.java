package org.isfce.pid.dao;

import java.util.List;
import java.util.Optional;

import org.isfce.pid.model.Dossier;
import org.isfce.pid.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository Spring Data pour les dossiers de dispense.
 *
 * @author Ludovic
 */
public interface IDossierDao extends JpaRepository<Dossier, Long> {
	List<Dossier> findDossierByUserOrderByDateCreationDesc(User user);

	@Query("from TDOSSIER d where d.user=:user and d.etat NOT IN (org.isfce.pid.model.EtatDossier.CLOTURE_ACCORDE, org.isfce.pid.model.EtatDossier.CLOTURE_REFUSE)")
	Optional<Dossier> findDossierEnCours(@Param("user") User user);

	@Query("SELECT COUNT(d) FROM TDOSSIER d WHERE d.user.username = :username AND d.etat NOT IN (org.isfce.pid.model.EtatDossier.CLOTURE_ACCORDE, org.isfce.pid.model.EtatDossier.CLOTURE_REFUSE)")
	int getNbDossierEnCours(@Param("username") String userName);

	@Query("SELECT COUNT(d) FROM TDOSSIER d WHERE d.user.username = :username AND d.etat IN (org.isfce.pid.model.EtatDossier.CLOTURE_ACCORDE, org.isfce.pid.model.EtatDossier.CLOTURE_REFUSE)")
	int getNbDossierCloture(@Param("username") String userName);

	int countByUser(User user);

	List<Dossier> findByUserUsernameOrderByDateCreationDesc(String username);
}
