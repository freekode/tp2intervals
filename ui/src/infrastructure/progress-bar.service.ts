import { Injectable } from '@angular/core';
import { Observable, ReplaySubject } from "rxjs";


@Injectable({
  providedIn: 'root'
})
export class ProgressBarService {
  private status = new ReplaySubject<boolean>()

  start() {
    this.status.next(true)
  }

  stop() {
    this.status.next(false)
  }

  subscribe(): Observable<boolean> {
    return this.status
  }
}
