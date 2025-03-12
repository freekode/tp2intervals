import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from 'rxjs';


@Injectable({
  providedIn: 'root'
})
export class WorkoutClient {

  constructor(private httpClient: HttpClient) {
  }

  copyCalendarToCalendar(startDate, endDate, types, skipSynced, platformDirection): Observable<any> {
    return this.httpClient
      .post(`/api/workout/copy-calendar-to-calendar`, {startDate, endDate, types, skipSynced, ...platformDirection})
  }

  copyCalendarToLibrary(name, startDate, endDate, types, platformDirection, isPlan): Observable<any> {
    return this.httpClient
      .post(`/api/workout/copy-calendar-to-library`, {name, startDate, endDate, types, ...platformDirection, isPlan})
  }

  copyLibraryToLibrary(externalData, targetLibraryContainer, platformDirection): Observable<any> {
    return this.httpClient
      .post(`/api/workout/copy-library-to-library`, {
        workoutExternalData: externalData,
        targetLibraryContainer, ...platformDirection
      })
  }

  findWorkoutsByName(platform, name): Observable<any> {
    return this.httpClient.get(`/api/workout/find`, {params: {platform, name}})
  }

  scheduleCopyCalendarToCalendar(startDate, endDate, types, skipSynced, platformDirection): Observable<any> {
    return this.httpClient
      .post(`/api/workout/copy-calendar-to-calendar/schedule`, {
        startDate,
        endDate,
        types,
        skipSynced,
        ...platformDirection
      })
  }

  getScheduleRequests(): Observable<any> {
    return this.httpClient.get(`/api/workout/copy-calendar-to-calendar/schedule`)
  }

  deleteScheduleRequest(id: any) {
    return this.httpClient.delete(`/api/workout/copy-calendar-to-calendar/schedule/${id}`)
  }
}
