import { Injectable } from '@angular/core';
import { Observable } from "rxjs";
import { ServiceClient } from "./service.client";


@Injectable({
  providedIn: 'root'
})
export class WorkoutService {
  constructor(private serviceClient: ServiceClient) {
  }

  planWorkout(): Observable<any> {
    return this.serviceClient.planWorkout()
  }

  copyPlan(startDate, endDate): Observable<any> {
    return this.serviceClient.copyPlan({startDate, endDate})
  }
}
