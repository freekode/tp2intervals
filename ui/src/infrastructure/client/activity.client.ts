import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { Observable } from 'rxjs';


@Injectable({
  providedIn: 'root'
})
export class ActivityClient {

  constructor(private httpClient: HttpClient) {
  }

  syncActivities(startDate, endDate, types): Observable<any> {
    return this.httpClient
      .post(`/api/activity/sync/TRAINER_ROAD/INTERVALS`, {startDate, endDate, types})
  }
}
