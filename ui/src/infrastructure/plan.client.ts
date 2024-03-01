import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { map, Observable } from 'rxjs';


@Injectable({
  providedIn: 'root'
})
export class PlanClient {

  constructor(private httpClient: HttpClient) {
  }

  getPlans(platform: string): Observable<any[]> {
    return this.httpClient.get(`/api/plan`, {params: {platform}}).pipe(
      map(plans => (<any[]>plans))
    )
  }

  copyPlan(plan, platformDirection): Observable<any> {
    return this.httpClient
      .post(`/api/plan/copy`, {plan, ...platformDirection})
  }
}
