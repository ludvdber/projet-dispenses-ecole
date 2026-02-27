package org.isfce.pid.service;

import java.time.LocalDate;
import java.util.Optional;

import org.isfce.pid.controller.error.DossierException;
import org.isfce.pid.dao.IDossierDao;
import org.isfce.pid.model.Dossier;
import org.isfce.pid.model.EtatDossier;
import org.isfce.pid.model.NbDossiers;
import org.isfce.pid.model.User;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;

// L'analyse null d'Eclipse génère des faux positifs sur les retours de Spring Data JPA
// (save(), findById()…) dont les @NonNull ne sont pas toujours lus depuis le classpath.
@SuppressWarnings("null")
@Service
@AllArgsConstructor
public class DossierService {
	private IDossierDao daoDossier;

	/**
	 * Crée un dossier si le user n'as pas de dossier en cours
	 * @param user
	 * @return
	 * @throws DossierException si possède déjà un dossier
	 */
	public Dossier createDossier(User user, String objetDemande) throws DossierException {
		// vérifie s'il existe un dossier en cours ==> exception
		if (daoDossier.getNbDossierEnCours(user.getUsername()) > 0) {
			throw new DossierException("err.dossier.enCours");
		}
		Dossier dossier = Dossier.builder().dateCreation(LocalDate.now()).etat(EtatDossier.DEMANDE_EN_COURS).user(user)
				.objetDemande(objetDemande).build();
		dossier = daoDossier.save(dossier);
		return dossier;
	}

	public Optional<Dossier> getDossierEnCours(User user) {
		return daoDossier.findDossierEnCours(user);
	}

	public int getNbDossier(User user) {
		return daoDossier.countByUser(user);
	}

	public NbDossiers getNbDossiers(String userName) {
		return new NbDossiers(daoDossier.getNbDossierCloture(userName), daoDossier.getNbDossierEnCours(userName));

	}

}
