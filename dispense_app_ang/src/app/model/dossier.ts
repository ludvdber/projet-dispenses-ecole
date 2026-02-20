import {EtatDossier} from './etat-dossier';
import {User} from './user';

export interface Dossier {
  id: number;
  date: Date;
  etat: EtatDossier
  objetDemande:string;
  user: User
}
