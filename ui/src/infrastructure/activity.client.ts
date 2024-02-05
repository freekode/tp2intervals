import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { Observable } from 'rxjs';


@Injectable({
  providedIn: 'root'
})
export class ActivityClient {

  constructor(private httpClient: HttpClient) {
  }

  syncActivities(startDate, endDate): Observable<any> {
    return this.httpClient
      .post(`/api/activity/sync/TRAINER_ROAD/INTERVALS`, {startDate, endDate})
  }

  startJobSyncActivities(): Observable<any> {
    return this.httpClient
      .post(`/api/activity/sync/TRAINER_ROAD/INTERVALS/job`, {})
  }

  getJobSyncActivities(): Observable<any> {
    return this.httpClient
      .get(`/api/activity/sync/TRAINER_ROAD/INTERVALS/job`)
  }

  stopJobSyncActivities(): Observable<any> {
    return this.httpClient
      .delete(`/api/activity/sync/TRAINER_ROAD/INTERVALS/job`)
  }
}
