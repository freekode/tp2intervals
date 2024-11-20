import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from 'rxjs';


@Injectable({
  providedIn: 'root'
})
export class ActivityClient {

  constructor(private httpClient: HttpClient) {
  }

  copyActivities(startDate, endDate, types, platformDirection): Observable<any> {
    return this.httpClient
      .post(`/api/activities/copy`, {startDate, endDate, types, ...platformDirection})
  }
}
