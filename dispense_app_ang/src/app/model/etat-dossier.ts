export enum EtatDossier {
  DEMANDE_EN_COURS = 'DEMANDE_EN_COURS',
  TRAITEMENT_DIRECTION = 'TRAITEMENT_DIRECTION',
  TRAITEMENT_ENSEIGNANT = 'TRAITEMENT_ENSEIGNANT',
  ATTENTE_COMPLEMENT = 'ATTENTE_COMPLEMENT',
  CLOTURE_ACCORDE = 'CLOTURE_ACCORDE',
  CLOTURE_REFUSE = 'CLOTURE_REFUSE'
}

export const ETAT_DOSSIER_LABELS: Record<EtatDossier, string> = {
  [EtatDossier.DEMANDE_EN_COURS]: 'En cours de saisie',
  [EtatDossier.TRAITEMENT_DIRECTION]: 'En traitement (direction)',
  [EtatDossier.TRAITEMENT_ENSEIGNANT]: 'En traitement (enseignant)',
  [EtatDossier.ATTENTE_COMPLEMENT]: 'Complément demandé',
  [EtatDossier.CLOTURE_ACCORDE]: 'Accordé',
  [EtatDossier.CLOTURE_REFUSE]: 'Refusé'
};

export function etatLabel(etat: EtatDossier): string {
  return ETAT_DOSSIER_LABELS[etat] ?? etat;
}

export function etatColor(etat: EtatDossier): string {
  switch (etat) {
    case EtatDossier.DEMANDE_EN_COURS: return 'etat-bleu';
    case EtatDossier.TRAITEMENT_DIRECTION:
    case EtatDossier.TRAITEMENT_ENSEIGNANT: return 'etat-orange';
    case EtatDossier.ATTENTE_COMPLEMENT: return 'etat-rouge';
    case EtatDossier.CLOTURE_ACCORDE: return 'etat-vert';
    case EtatDossier.CLOTURE_REFUSE: return 'etat-gris';
    default: return '';
  }
}

export function etatMessage(etat: EtatDossier): string {
  switch (etat) {
    case EtatDossier.TRAITEMENT_DIRECTION: return 'En attente de traitement par la direction';
    case EtatDossier.TRAITEMENT_ENSEIGNANT: return 'En cours d\'évaluation par le coordinateur';
    case EtatDossier.ATTENTE_COMPLEMENT: return 'Complément demandé — vous pouvez modifier votre dossier';
    case EtatDossier.CLOTURE_ACCORDE: return 'Dispenses accordées \u2713';
    case EtatDossier.CLOTURE_REFUSE: return 'Demande refusée';
    default: return '';
  }
}
