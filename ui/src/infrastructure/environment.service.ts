import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {map, Observable} from 'rxjs';


@Injectable({
  providedIn: 'root'
})
export class EnvironmentService {
  readonly defaultPort = 8080;
  readonly electronPort = 44864;

  constructor(private httpClient: HttpClient) {
  }

  getAddress() {
    let port = window.electron ? this.electronPort : this.defaultPort
    return `http://localhost:${port}`
  }

  getVersion(): Observable<string> {
    return this.httpClient.get(`/actuator/info`).pipe(
      map((response: any) => response.build.version)
    )
  }
}
