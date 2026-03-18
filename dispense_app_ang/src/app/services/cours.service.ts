import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {environment} from '../../environments/environment';
import {CoursEtudiant} from '../model/cours-etudiant';

/**
 * Service HTTP pour les opérations sur les cours de l'étudiant.
 * @author Ludovic
 */
@Injectable({
  providedIn: 'root',
})
export class CoursService {
  private readonly http = inject(HttpClient);
  private readonly api = environment.resourceServer.url;

  addCours(cours: CoursEtudiant): Observable<CoursEtudiant> {
    return this.http.post<CoursEtudiant>(`${this.api}/api/cours-etudiant/add`, cours);
  }

  deleteCours(id: number): Observable<void> {
    return this.http.delete<void>(`${this.api}/api/cours-etudiant/${id}`);
  }

  getCoursParDossier(dossierId: number): Observable<CoursEtudiant[]> {
    return this.http.get<CoursEtudiant[]>(`${this.api}/api/cours-etudiant/dossier/${dossierId}`);
  }
}
