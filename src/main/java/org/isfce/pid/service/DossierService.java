package org.isfce.pid.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.isfce.pid.controller.error.DossierException;
import org.isfce.pid.dao.IDispenseCoursDao;
import org.isfce.pid.dao.IDispenseDao;
import org.isfce.pid.dao.IDocumentDao;
import org.isfce.pid.dao.IDossierDao;
import org.isfce.pid.dao.IUserDao;
import org.isfce.pid.dto.CompletudeDossier;
import org.isfce.pid.dto.DossierDto;
import org.isfce.pid.mapper.UEMapper;
import org.isfce.pid.model.CoursEtudiant;
import org.isfce.pid.model.Dispense;
import org.isfce.pid.model.DispenseCours;
import org.isfce.pid.model.Dossier;
import org.isfce.pid.model.EtatDossier;
import org.isfce.pid.model.StatutSaisie;
import org.isfce.pid.model.TypeDoc;
import org.isfce.pid.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.AllArgsConstructor;

/**
 * Service de gestion des dossiers de dispense (création, complétude, soumission).
 * @author Ludovic
 */
// L'analyse null d'Eclipse génère des faux positifs sur les retours de Spring Data JPA
// (save(), findById()…) dont les @NonNull ne sont pas toujours lus depuis le classpath.
@SuppressWarnings("null")
@Service
@AllArgsConstructor
@Transactional
public class DossierService {
	private IDossierDao daoDossier;
	private IUserDao daoUser;
	private IDispenseDao daoDispense;
	private IDispenseCoursDao daoDispenseCours;
	private IDocumentDao daoDocument;
	private UEMapper mapper;

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

	/**
	 * Crée un dossier à partir du username.
	 * Vérifie qu'il n'y a pas de dossier actif pour cet étudiant.
	 */
	public DossierDto createDossier(String objetDemande, String username) throws DossierException {
		User user = daoUser.findById(username)
				.orElseThrow(() -> new DossierException("err.dossier.notFound"));
		Dossier dossier = createDossier(user, objetDemande);
		return mapper.toDossierDto(dossier);
	}

	public Optional<Dossier> getDossierEnCours(User user) {
		return daoDossier.findDossierEnCours(user);
	}

	/**
	 * Retourne tous les dossiers d'un utilisateur, triés par date de création décroissante.
	 */
	public List<DossierDto> getDossiers(String username) {
		return daoDossier.findByUserUsernameOrderByDateCreationDesc(username)
				.stream().map(mapper::toDossierDto).toList();
	}

	/**
	 * Retourne le détail d'un dossier par ID.
	 * Vérifie que le dossier appartient bien à l'utilisateur authentifié.
	 */
	public DossierDto getDossier(Long id, String username) throws DossierException {
		Dossier dossier = daoDossier.findById(id)
				.orElseThrow(() -> new DossierException("err.dossier.notFound"));
		if (!dossier.getUser().getUsername().equals(username)) {
			throw new DossierException("err.dossier.forbidden");
		}
		return mapper.toDossierDto(dossier);
	}

	/**
	 * Vérifie la complétude d'un dossier selon les 4 règles métier.
	 */
	public CompletudeDossier checkCompletude(Long dossierId) {
		// Règle 1 : >= 1 bulletin actif
		boolean bulletinOk = daoDocument.countByDossierIdAndTypeDocAndDeletedAtIsNull(
				dossierId, TypeDoc.BULLETIN) >= 1;

		// Règle 2 : >= 1 motivation active
		boolean motivationOk = daoDocument.countByDossierIdAndTypeDocAndDeletedAtIsNull(
				dossierId, TypeDoc.MOTIVATION) >= 1;

		// Règle 3 : >= 1 dispense avec >= 1 cours justificatif
		List<Dispense> dispenses = daoDispense.findByDossierId(dossierId);
		boolean dispensesOk = false;
		for (Dispense d : dispenses) {
			List<DispenseCours> liens = daoDispenseCours.findByDispenseId(d.getId());
			if (!liens.isEmpty()) {
				dispensesOk = true;
				break;
			}
		}

		// Règle 4 : chaque cours INCONNU lié à une dispense doit avoir URL_FICHE ou document PROGRAMME_COURS
		boolean coursInconnusOk = true;
		boolean hasCoursInconnus = false;
		for (Dispense d : dispenses) {
			for (DispenseCours dc : daoDispenseCours.findByDispenseId(d.getId())) {
				CoursEtudiant cours = dc.getCoursEtudiant();
				if (cours.getStatutSaisie() == StatutSaisie.INCONNU) {
					hasCoursInconnus = true;
					boolean hasUrl = cours.getUrlFiche() != null && !cours.getUrlFiche().isBlank();
					boolean hasDoc = daoDocument.countByCoursEtudiantIdAndTypeDocAndDeletedAtIsNull(
							cours.getId(), TypeDoc.PROGRAMME_COURS) >= 1;
					if (!hasUrl && !hasDoc) {
						coursInconnusOk = false;
						break;
					}
				}
			}
			if (!coursInconnusOk) break;
		}

		boolean complet = bulletinOk && motivationOk && dispensesOk && coursInconnusOk;
		return new CompletudeDossier(complet, bulletinOk, motivationOk, dispensesOk, coursInconnusOk, hasCoursInconnus);
	}

	/**
	 * Soumet un dossier : vérifie ownership, état, complétude, puis change l'état.
	 */
	public DossierDto submitDossier(Long dossierId, String username) throws DossierException {
		Dossier dossier = daoDossier.findById(dossierId)
				.orElseThrow(() -> new DossierException("err.dossier.notFound"));
		if (!dossier.getUser().getUsername().equals(username)) {
			throw new DossierException("err.dossier.forbidden");
		}

		// Seuls DEMANDE_EN_COURS et ATTENTE_COMPLEMENT permettent la soumission
		if (dossier.getEtat() != EtatDossier.DEMANDE_EN_COURS
				&& dossier.getEtat() != EtatDossier.ATTENTE_COMPLEMENT) {
			throw new DossierException("err.dossier.dejaEnTraitement");
		}

		CompletudeDossier completude = checkCompletude(dossierId);
		if (!completude.complet()) {
			throw new DossierException("err.dossier.incomplet");
		}

		dossier.setEtat(EtatDossier.TRAITEMENT_DIRECTION);
		dossier.setComplet(true);
		dossier.setDateSoumis(LocalDate.now());
		dossier = daoDossier.save(dossier);
		return mapper.toDossierDto(dossier);
	}

}
