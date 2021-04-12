import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";

const baseUrl = "http://118.67.133.84:8085/api"

@Injectable({
  providedIn: 'root'
})
export class WebsitesService {

  constructor(private http: HttpClient) { }

  getDates(website: any): Observable<any> {
    return this.http.get(`${baseUrl}/websites/dates?web=${website}`);
  }

  getComparison(website: any, prev: any, next: any): Observable<any> {
    return this.http.get(`${baseUrl}/comparison?web=${website}&prev=${prev}&next=${next}`);
  }

  getChanges(website: any, prev: any): Observable<string> {
    return this.http.get<string>(`${baseUrl}/comparison?web=${website}&prev=${prev}`);
  }

}
