export interface CoursEtudiant {
  id?: number;
  dossierId: number;
  codeEcole?: string;
  nomEcole?: string;
  codeCours: string;
  intitule: string;
  ects?: number;
  urlFiche?: string;
  corrCoursId?: number;
  statutSaisie?: 'AUTO_RECONNU' | 'INCONNU';
}
