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

  copyWorkouts(startDate, endDate, types): Observable<any> {
    return this.httpClient
      .post(`/api/workout/copy/TRAINING_PEAKS/INTERVALS`, {startDate, endDate, types})
  }

  startJobPlanWorkout(): Observable<any> {
    return this.httpClient
      .post(`/api/workout/plan/INTERVALS/TRAINING_PEAKS/job`, {})
  }

  getJobPlanWorkout(): Observable<any> {
    return this.httpClient
      .get(`/api/workout/plan/INTERVALS/TRAINING_PEAKS/job`)
  }

  stopJobPlanWorkout(): Observable<any> {
    return this.httpClient
      .delete(`/api/workout/plan/INTERVALS/TRAINING_PEAKS/job`)
  }
}
