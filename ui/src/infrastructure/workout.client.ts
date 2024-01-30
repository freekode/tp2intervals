import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { Observable } from 'rxjs';


@Injectable({
  providedIn: 'root'
})
export class WorkoutClient {

  constructor(private httpClient: HttpClient) {
  }

  planWorkout(): Observable<any> {
    return this.httpClient
      .post(`/api/workout/plan-workout/INTERVALS/TRAINING_PEAKS`, {})
  }

  startJobPlanWorkout(): Observable<any> {
    return this.httpClient
      .post(`/api/workout/plan-workout/INTERVALS/TRAINING_PEAKS/job`, {})
  }

  getJobPlanWorkout(): Observable<any> {
    return this.httpClient
      .get(`/api/workout/plan-workout/INTERVALS/TRAINING_PEAKS/job`)
  }

  stopJobPlanWorkout(): Observable<any> {
    return this.httpClient
      .delete(`/api/workout/plan-workout/INTERVALS/TRAINING_PEAKS/job`)
  }

  copyPlan(startDate, endDate) {
    return this.httpClient
      .post(`/api/workout/copy-plan/TRAINING_PEAKS/INTERVALS`, {startDate, endDate})
  }
}
