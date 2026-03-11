import {CoursEtudiant} from './cours-etudiant';

export interface Dispense {
  id?: number;
  dossierId: number;
  codeUe: string;
  nomUe?: string;
  decision?: string;
  coursJustificatifs?: CoursEtudiant[];
  correspondanceReconnue?: boolean;
}
