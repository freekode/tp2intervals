import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { Observable } from 'rxjs';
import { ConfigData } from "./config-data";


@Injectable({
  providedIn: 'root'
})
export class ConfigurationClient {

  constructor(private httpClient: HttpClient) {
  }

  getConfig(): Observable<any> {
    return this.httpClient
      .get(`/api/configuration`)
  }

  updateConfig(configData: ConfigData): Observable<any> {
    return this.httpClient
      .put(`/api/configuration`, configData)
  }
}
