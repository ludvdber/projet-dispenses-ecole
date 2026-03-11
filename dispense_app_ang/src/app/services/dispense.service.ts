import {inject, Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {environment} from '../../environments/environment';
import {Section} from '../model/section';
import {Observable} from 'rxjs';
import {Ue} from '../model/ue';
import {UeFullDto} from '../model/ue-full-dto';

@Injectable({
  providedIn: 'root',
})
export class DispenseService {
  private http = inject(HttpClient);
  private readonly urlListeSections = `${environment.resourceServer.url}/api/ue/sections`;
  private readonly urlListeUEBySection = `${environment.resourceServer.url}/api/ue/liste`;
  private readonly urlUeDetail = `${environment.resourceServer.url}/api/ue/detail`;

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
}
