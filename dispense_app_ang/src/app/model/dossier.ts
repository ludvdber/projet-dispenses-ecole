import {EtatDossier} from './etat-dossier';
import {User} from './user';

export interface Dossier {
  id: number;
  dateCreation: Date;
  dateSoumis?: Date;
  etat: EtatDossier;
  objetDemande: string;
  complet: boolean;
  user: User;
}
