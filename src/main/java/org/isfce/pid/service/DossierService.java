package org.isfce.pid.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.isfce.pid.exception.DossierException;
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

import lombok.RequiredArgsConstructor;

/**
 * Service de gestion des dossiers de dispense (création, complétude, soumission).
 * @author Ludovic
 */

@SuppressWarnings("null")
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DossierService {
	/** États terminaux (clôturés) — un dossier actif est tout dossier hors de ces états. */
	private static final Set<EtatDossier> ETATS_CLOTURES = Set.of(
			EtatDossier.CLOTURE_ACCORDE, EtatDossier.CLOTURE_REFUSE);

	private final IDossierDao daoDossier;
	private final IUserDao daoUser;
	private final IDispenseDao daoDispense;
	private final IDispenseCoursDao daoDispenseCours;
	private final IDocumentDao daoDocument;
	private final UEMapper mapper;

	/**
	 * Crée un dossier si le user n'as pas de dossier en cours
	 * @param user
	 * @return
	 * @throws DossierException si possède déjà un dossier
	 */
	@Transactional
	public Dossier createDossier(User user, String objetDemande) {
		// vérifie s'il existe un dossier en cours ==> exception
		if (daoDossier.countByUserUsernameAndEtatNotIn(user.getUsername(), ETATS_CLOTURES) > 0) {
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
	@Transactional
	public DossierDto createDossier(String objetDemande, String username) {
		User user = daoUser.findById(username)
				.orElseThrow(() -> new DossierException("err.dossier.notFound"));
		Dossier dossier = createDossier(user, objetDemande);
		return mapper.toDossierDto(dossier);
	}

	public Optional<Dossier> getDossierEnCours(User user) {
		return daoDossier.findFirstByUserAndEtatNotIn(user, ETATS_CLOTURES);
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
	public DossierDto getDossier(Long id, String username) {
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
		boolean bulletinOk = hasBulletin(dossierId);
		boolean motivationOk = hasMotivation(dossierId);
		List<Dispense> dispenses = daoDispense.findByDossierId(dossierId);
		boolean dispensesOk = hasDispenseAvecCours(dispenses);
		CoursInconnusResult coursInconnusResult = checkCoursInconnus(dispenses);

		boolean complet = bulletinOk && motivationOk && dispensesOk && coursInconnusResult.ok;
		return new CompletudeDossier(complet, bulletinOk, motivationOk, dispensesOk,
				coursInconnusResult.ok, coursInconnusResult.hasCoursInconnus);
	}

	private boolean hasBulletin(Long dossierId) {
		return daoDocument.countByDossierIdAndTypeDocAndDeletedAtIsNull(
				dossierId, TypeDoc.BULLETIN) >= 1;
	}

	private boolean hasMotivation(Long dossierId) {
		return daoDocument.countByDossierIdAndTypeDocAndDeletedAtIsNull(
				dossierId, TypeDoc.MOTIVATION) >= 1;
	}

	private boolean hasDispenseAvecCours(List<Dispense> dispenses) {
		for (Dispense d : dispenses) {
			if (!daoDispenseCours.findByDispenseId(d.getId()).isEmpty()) {
				return true;
			}
		}
		return false;
	}

	private record CoursInconnusResult(boolean ok, boolean hasCoursInconnus) {}

	private CoursInconnusResult checkCoursInconnus(List<Dispense> dispenses) {
		boolean ok = true;
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
						ok = false;
						break;
					}
				}
			}
			if (!ok) break;
		}
		return new CoursInconnusResult(ok, hasCoursInconnus);
	}

	/**
	 * Soumet un dossier : vérifie ownership, état, complétude, puis change l'état.
	 */
	@Transactional
	public DossierDto submitDossier(Long dossierId, String username) {
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
