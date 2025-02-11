import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {map, Observable} from 'rxjs';


@Injectable({
  providedIn: 'root'
})
export class EnvironmentService {
  readonly electronPort = 44864;

  constructor(private httpClient: HttpClient) {
  }

  getAddress() {
    if (window.electron) {
      return `http://localhost:${this.electronPort}`
    } else {
      return ''
    }
  }

  getVersion(): Observable<string> {
    return this.httpClient.get(`/actuator/info`).pipe(
      map((response: any) => response.build.version)
    )
  }
}
