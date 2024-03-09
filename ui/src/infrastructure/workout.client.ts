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

  copyPlannedWorkouts(name, startDate, endDate, types, platformDirection, isPlan): Observable<any> {
    return this.httpClient
      .post(`/api/workout/copy`, {name, startDate, endDate, types, ...platformDirection, isPlan})
  }

  findWorkoutsByName(platform, name): Observable<any> {
    return this.httpClient.get(`/api/workout/find`, {params: {platform, name}})
  }

}
