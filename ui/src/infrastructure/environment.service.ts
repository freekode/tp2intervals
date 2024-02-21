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
    return window.electron?.bootAddress || 'http://localhost:8080'
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
}
