export interface DocumentDossier {
  id?: number;
  dossierId?: number;
  coursEtudiantId?: number;
  typeDoc: 'BULLETIN' | 'PROGRAMME_COURS' | 'MOTIVATION';
  originalFilename?: string;
  typeMime?: string;
  taille?: number;
  dateDepot?: string;
  hashSha256?: string;
}
