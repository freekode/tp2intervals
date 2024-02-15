import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { Observable } from 'rxjs';


@Injectable({
  providedIn: 'root'
})
export class WorkoutClient {

  constructor(private httpClient: HttpClient) {
  }

  planWorkout(direction, startDate, endDate, types): Observable<any> {
    return this.httpClient
      .post(`/api/workout/plan/${direction}`, {startDate, endDate, types})
  }

  copyWorkouts(name, startDate, endDate, types): Observable<any> {
    return this.httpClient
      .post(`/api/workout/copy/TRAINING_PEAKS/INTERVALS`, {name, startDate, endDate, types})
  }
}
