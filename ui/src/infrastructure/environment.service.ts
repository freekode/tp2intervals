import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {map, Observable} from 'rxjs';


@Injectable({
  providedIn: 'root'
})
export class EnvironmentService {
  readonly port = 44864;
  readonly address = `http://localhost:${this.port}`;

  constructor(private httpClient: HttpClient) {
  }

  getVersion(): Observable<string> {
    return this.httpClient.get(`/actuator/info`).pipe(
      map((response: any) => response.build.version)
    )
  }
}
