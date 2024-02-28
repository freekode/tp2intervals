import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { Observable } from 'rxjs';


@Injectable({
  providedIn: 'root'
})
export class WorkoutClient {

  constructor(private httpClient: HttpClient) {
  }

  planWorkout(startDate, endDate, types, skipSynced, platformDirection): Observable<any> {
    return this.httpClient
      .post(`/api/workout/plan`, {startDate, endDate, types, skipSynced, ...platformDirection})
  }

  copyWorkouts(name, startDate, endDate, types, platformDirection): Observable<any> {
    return this.httpClient
      .post(`/api/workout/copy`, {name, startDate, endDate, types, ...platformDirection})
  }
}
