import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { map, Observable } from 'rxjs';
import { ConfigData } from "../config-data";


@Injectable({
  providedIn: 'root'
})
export class ConfigurationClient {

  constructor(private httpClient: HttpClient) {
  }

  getConfig(): Observable<ConfigData> {
    return this.httpClient.get(`/api/configuration`).pipe(
      map((response: any) =>
        new ConfigData(response?.config))
    )
  }

  updateConfig(configData: ConfigData): Observable<any> {
    return this.httpClient
      .put(`/api/configuration`, configData)
  }

  getAllPlatformInfo(): Observable<any> {
    return this.httpClient.get(`/api/configuration/platform`).pipe(
      map(response => {
        Object.keys(response).forEach(key => response[key] = response[key].infoMap)
        return response
      })
    )
  }

  platformInfo(platform): Observable<any> {
    return this.httpClient.get(`/api/configuration/${platform}`).pipe(
      map(response => (<any>response).infoMap)
    )
  }
}
