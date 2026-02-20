/**
 * DTO complet d'une UE retourné par GET /api/ue/detail/{code},
 * incluant les acquis d'apprentissage.
 *
 * Miroir de {@code UEFullDto} et {@code AcquisFullDto} côté backend.
 *
 * @author Ludovic
 */

export interface AcquisDto {
  num: number;
  acquis: string;
  pourcentage: number;
}

export interface UeFullDto {
  code: string;
  ref: string;
  nom: string;
  nbPeriodes: number;
  ects: number;
  prgm: string;
  acquis: AcquisDto[];
}
