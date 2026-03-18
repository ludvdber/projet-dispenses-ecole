import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {environment} from '../../environments/environment';
import {DocumentDossier} from '../model/document-dossier';

/**
 * Service HTTP pour les opérations sur les documents d'un dossier.
 * @author Ludovic
 */
@Injectable({
  providedIn: 'root',
})
export class DocumentService {
  private readonly http = inject(HttpClient);
  private readonly api = environment.resourceServer.url;

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
}
