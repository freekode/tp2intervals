import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from 'rxjs';


export interface TrainingPeaksSyncPreview {
  sourceWorkoutId?: string;
  sourceDate: string;
  status: string;
  reason: string;
  candidates: {eventId: number; date?: string}[];
}

export interface ScheduledWorkoutRequest {
  id: number;
  request?: {types: string[]; skipSynced: boolean; sourcePlatform: string; targetPlatform: string};
  blockedReason?: string;
}

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

  previewTrainingPeaksSync(startDate: string, endDate: string, types: string[], token: string) {
    return this.httpClient.post<TrainingPeaksSyncPreview[]>(
      '/api/workout/trainingpeaks-sync/preview',
      {startDate, endDate, types},
      {headers: {Authorization: 'Bearer ' + token}}
    );
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

  getScheduleRequests(): Observable<ScheduledWorkoutRequest[]> {
    return this.httpClient.get<ScheduledWorkoutRequest[]>(`/api/workout/copy-calendar-to-calendar/schedule`)
  }

  deleteScheduleRequest(id: any) {
    return this.httpClient.delete(`/api/workout/copy-calendar-to-calendar/schedule/${id}`)
  }
}
