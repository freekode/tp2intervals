import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { Observable } from 'rxjs';
import { ConfigData } from "./config-data";


@Injectable({
  providedIn: 'root'
})
export class ServiceClient {

  constructor(private httpClient: HttpClient) {
  }

  getConfig(): Observable<any> {
    return this.httpClient
      .get(`/api/config`)
  }

  updateConfig(configData: ConfigData): Observable<any> {
    return this.httpClient
      .put(`/api/config`, configData)
  }

  planWorkout(): Observable<any> {
    return this.httpClient
      .post(`/api/plan-workout`, {})
  }

  copyPlan(data) {
    return this.httpClient
      .post(`/api/copy-plan`, data)
  }
}
