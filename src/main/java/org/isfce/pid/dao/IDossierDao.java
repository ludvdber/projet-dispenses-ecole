package org.isfce.pid.dao;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.isfce.pid.model.Dossier;
import org.isfce.pid.model.EtatDossier;
import org.isfce.pid.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository Spring Data pour les dossiers de dispense.
 *
 * @author Ludovic
 */
public interface IDossierDao extends JpaRepository<Dossier, Long> {
	List<Dossier> findDossierByUserOrderByDateCreationDesc(User user);

	Optional<Dossier> findFirstByUserAndEtatNotIn(User user, Collection<EtatDossier> etats);

	int countByUserUsernameAndEtatNotIn(String username, Collection<EtatDossier> etats);

	int countByUser(User user);

	List<Dossier> findByUserUsernameOrderByDateCreationDesc(String username);
}
