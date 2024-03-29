import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { map, Observable, of } from 'rxjs';


@Injectable({
  providedIn: 'root'
})
export class EnvironmentService {

  constructor(private httpClient: HttpClient) {
  }

  getBootAddress() {
    return window.electron?.bootAddress || ''
  }

  getVersion(): Observable<string> {
    if (window.electron) {
      return of(window.electron.appVersion)
    } else {
      return this.httpClient.get(`/actuator/info`).pipe(
        map((response: any) => response.build.version)
      )
    }
  }

  setLoggerLevel(packageName, level): Observable<any> {
    return this.httpClient.post(`/actuator/loggers/${packageName}`, {configuredLevel: level})
  }

  getLoggerLevel(packageName): Observable<string> {
    return this.httpClient.get(`/actuator/loggers/${packageName}`).pipe(
      map((response: any) => response.effectiveLevel)
    )
  }

}
