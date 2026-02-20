export enum EtatDossier {
  DEMANDE_EN_COURS = 'DEMANDE_EN_COURS',
  TRAITEMENT_DIRECTION = 'TRAITEMENT_DIRECTION',
  TRAITEMENT_PROFESSEUR = 'TRAITEMENT_PROFESSEUR',
  ATTENTE_COMPLEMENT = 'ATTENTE_COMPLEMENT',
  TRAITE = 'TRAITE',
  CLOTURE = 'CLOTURE'
}

export const ETAT_DOSSIER_LABELS: Record<EtatDossier, string> = {
  [EtatDossier.DEMANDE_EN_COURS]: 'Nouveau dossier',
  [EtatDossier.TRAITEMENT_DIRECTION]: 'Direction',
  [EtatDossier.TRAITEMENT_PROFESSEUR]: 'Professeur',
  [EtatDossier.ATTENTE_COMPLEMENT]: 'Incomplet',
  [EtatDossier.TRAITE]: 'Traité',
  [EtatDossier.CLOTURE]: 'Clôturé'
};
