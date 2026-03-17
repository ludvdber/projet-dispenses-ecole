import {inject, Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {environment} from '../../environments/environment';
import {Dossier} from '../model/dossier';
import {CompletudeDossier} from '../model/completude-dossier';
import {CoursEtudiant} from '../model/cours-etudiant';
import {Dispense} from '../model/dispense';
import {DocumentDossier} from '../model/document-dossier';
import {AnalyseDto} from '../model/analyse-dto';
import {Ecole} from '../model/ecole';
import {CoursEcole} from '../model/cours-ecole';
import {Ue} from '../model/ue';

/**
 * Service HTTP pour les opérations sur les dossiers, cours, dispenses, documents et analyses.
 * @author Ludovic
 */
@Injectable({
  providedIn: 'root',
})
export class DossierService {
  private http = inject(HttpClient);
  private readonly api = environment.resourceServer.url;

  // ======================== Dossier ========================

  createDossier(objetDemande: string): Observable<Dossier> {
    const params = new HttpParams().set('objetDemande', objetDemande);
    return this.http.post<Dossier>(`${this.api}/api/dossier/create`, null, {params});
  }

  getDossiers(): Observable<Dossier[]> {
    return this.http.get<Dossier[]>(`${this.api}/api/dossier/list`);
  }

  getDossier(id: number): Observable<Dossier> {
    return this.http.get<Dossier>(`${this.api}/api/dossier/${id}`);
  }

  getCompletude(id: number): Observable<CompletudeDossier> {
    return this.http.get<CompletudeDossier>(`${this.api}/api/dossier/${id}/completude`);
  }

  submitDossier(id: number): Observable<Dossier> {
    return this.http.put<Dossier>(`${this.api}/api/dossier/${id}/submit`, null);
  }

  // ======================== Cours étudiant ========================

  addCours(cours: CoursEtudiant): Observable<CoursEtudiant> {
    return this.http.post<CoursEtudiant>(`${this.api}/api/cours-etudiant/add`, cours);
  }

  deleteCours(id: number): Observable<void> {
    return this.http.delete<void>(`${this.api}/api/cours-etudiant/${id}`);
  }

  getCoursParDossier(dossierId: number): Observable<CoursEtudiant[]> {
    return this.http.get<CoursEtudiant[]>(`${this.api}/api/cours-etudiant/dossier/${dossierId}`);
  }

  // ======================== Dispenses ========================

  createDispense(dossierId: number, codeUe: string): Observable<Dispense> {
    const params = new HttpParams().set('dossierId', dossierId).set('codeUe', codeUe);
    return this.http.post<Dispense>(`${this.api}/api/dispense/create`, null, {params});
  }

  deleteDispense(id: number): Observable<void> {
    return this.http.delete<void>(`${this.api}/api/dispense/${id}`);
  }

  lierCours(dispenseId: number, coursId: number): Observable<Dispense> {
    return this.http.post<Dispense>(`${this.api}/api/dispense/${dispenseId}/cours/${coursId}`, null);
  }

  delierCours(dispenseId: number, coursId: number): Observable<Dispense> {
    return this.http.delete<Dispense>(`${this.api}/api/dispense/${dispenseId}/cours/${coursId}`);
  }

  getDispensesParDossier(dossierId: number): Observable<Dispense[]> {
    return this.http.get<Dispense[]>(`${this.api}/api/dispense/dossier/${dossierId}`);
  }

  // ======================== Documents ========================

  uploadDocument(dossierId: number, typeDoc: string, file: File, coursEtudiantId?: number): Observable<DocumentDossier> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('typeDoc', typeDoc);
    formData.append('dossierId', String(dossierId));
    if (coursEtudiantId != null) {
      formData.append('coursEtudiantId', String(coursEtudiantId));
    }
    return this.http.post<DocumentDossier>(`${this.api}/api/document/upload`, formData);
  }

  softDeleteDocument(id: number): Observable<DocumentDossier> {
    return this.http.delete<DocumentDossier>(`${this.api}/api/document/${id}`);
  }

  getDocumentsParDossier(dossierId: number): Observable<DocumentDossier[]> {
    return this.http.get<DocumentDossier[]>(`${this.api}/api/document/dossier/${dossierId}`);
  }

  getDownloadUrl(id: number): string {
    return `${this.api}/api/document/${id}/download`;
  }

  // ======================== Analyse ========================

  getAnalyse(dossierId: number): Observable<AnalyseDto[]> {
    return this.http.get<AnalyseDto[]>(`${this.api}/api/analyse/dossier/${dossierId}`);
  }

  // ======================== Écoles ========================

  getEcoles(): Observable<Ecole[]> {
    return this.http.get<Ecole[]>(`${this.api}/api/ecole`);
  }

  getCoursEcole(codeEcole: string): Observable<CoursEcole[]> {
    return this.http.get<CoursEcole[]>(`${this.api}/api/ecole/${codeEcole}/cours`);
  }

  // ======================== UEs (liste complète) ========================

  getUes(): Observable<Ue[]> {
    return this.http.get<Ue[]>(`${this.api}/api/ue/liste`);
  }
}
