import {inject, Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {AuthService} from '../auth/auth.service';
import {environment} from '../../environments/environment';
import {Section} from '../model/section';
import {Observable} from 'rxjs';
import {Ue} from '../model/ue';
import {UeFullDto} from '../model/ue-full-dto';
import {NbDossier} from '../model/nb-dossier';
import {CreateDossier} from '../model/create-dossier';
import {Dossier} from '../model/dossier';

@Injectable({
  providedIn: 'root',
})
export class DispenseService {
  private http = inject(HttpClient);
  private auth = inject(AuthService);
  private readonly urlListeUes = `${environment.resourceServer.url}/api/ue/liste`;
  private readonly urlListeSections = `${environment.resourceServer.url}/api/ue/sections`;
  private readonly urlListeUEBySection = `${environment.resourceServer.url}/api/ue/liste`;
  private readonly urlUeDetail = `${environment.resourceServer.url}/api/ue/detail`;
  private readonly urlNbDossier = `${environment.resourceServer.url}/api/dossier/nb`;
  private readonly urlCreationDossier = `${environment.resourceServer.url}/api/dossier/add`;

  getSections(): Observable<Section[]> {
    return this.http.get<Section[]>(this.urlListeSections);
  }

  getUesBySection(section: string): Observable<Ue[]> {
    const params = new HttpParams().set('section', section);
    return this.http.get<Ue[]>(this.urlListeUEBySection, {params});
  }

  getUeDetail(code: string): Observable<UeFullDto> {
    return this.http.get<UeFullDto>(`${this.urlUeDetail}/${code}`);
  }

  getNbdossier() {
    return this.http.get<NbDossier>(this.urlNbDossier);
  }

  createDossier(objetDemande: string) {
    const payload: CreateDossier = { objetDemande };
    return this.http.post<Dossier>(this.urlCreationDossier, payload);
  }
}
