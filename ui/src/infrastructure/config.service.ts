import { Injectable } from '@angular/core';
import { ConfigData } from './config-data';
import { map, Observable } from "rxjs";
import { ConfigurationClient } from "./configuration.client";


@Injectable({
  providedIn: 'root'
})
export class ConfigService {
  constructor(private serviceClient: ConfigurationClient) {
  }

  getConfig(): Observable<ConfigData> {
    return this.serviceClient.getConfig().pipe(
      map(response =>
        new ConfigData(response?.config))
    )
  }

  updateConfig(data: ConfigData): Observable<void> {
    return this.serviceClient.updateConfig(data)
  }
}
