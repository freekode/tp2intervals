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
      .post(`/api/workout/plan-workout/intervals/training-peaks`, {})
  }

  startJobPlanWorkout(): Observable<any> {
    return this.httpClient
      .post(`/api/workout/plan-workout/intervals/training-peaks/job`, {})
  }

  getJobPlanWorkout(): Observable<any> {
    return this.httpClient
      .get(`/api/workout/plan-workout/intervals/training-peaks/job`)
  }

  stopJobPlanWorkout(): Observable<any> {
    return this.httpClient
      .delete(`/api/workout/plan-workout/intervals/training-peaks/job`)
  }

  copyPlan(data) {
    return this.httpClient
      .post(`/api/workout/copy-plan/training-peaks/intervals`, data)
  }
}
