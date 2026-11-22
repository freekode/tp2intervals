import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from 'rxjs';


export interface TrainingPeaksSyncPreview {
  sourceWorkoutId?: string;
  sourceDate?: string;
  status: string;
  reason: string;
  candidates: {eventId: number; date?: string}[];
  canResolveUnmatchedHold?: boolean;
  missingLinkRepair?: {oldEventId: number; replacement?: {eventId: number; date?: string}};
  repairBlockedReason?: string;
}

export interface TrainingPeaksPreviewSelection {
  startDate: string;
  endDate: string;
  types: string[];
}

export interface TrainingPeaksPreviewInventory {
  revision: string;
  sourceAccountId: string;
  destinationAthleteId: string;
  workouts: TrainingPeaksSyncPreview[];
  approvedAt?: string;
  previewOnly: boolean;
  activationEnabled: boolean;
}

export type ScheduleWindow = 'TODAY_AND_TOMORROW';

export interface ScheduleSettings {
  intervalMinutes: number;
  timezone: string;
  forwardEnabled: boolean;
  previewOnly: boolean;
  otherIntervalMinutes: number;
  otherTimezone: string;
}

export interface ScheduledWorkoutRequest {
  id: number;
  request?: {types: string[]; skipSynced: boolean; sourcePlatform: string; targetPlatform: string; window?: ScheduleWindow};
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

  previewTrainingPeaksSync(selection: TrainingPeaksPreviewSelection, token: string) {
    return this.httpClient.post<TrainingPeaksPreviewInventory>(
      '/api/workout/trainingpeaks-sync/preview',
      selection,
      {headers: {Authorization: 'Bearer ' + token}}
    );
  }

  approveTrainingPeaksBaseline(
    selection: TrainingPeaksPreviewSelection, revision: string, acknowledgeInconclusive: boolean,
    legacyLinks: Record<string, number>, token: string
  ) {
    return this.httpClient.post<{approvedAt: string; heldWorkouts: number}>(
      '/api/workout/trainingpeaks-sync/baseline/approve',
      {selection, revision, acknowledgeInconclusive, legacyLinks},
      {headers: {Authorization: 'Bearer ' + token}}
    );
  }

  resolveTrainingPeaksHold(
    selection: TrainingPeaksPreviewSelection, revision: string, sourceWorkoutId: string, token: string
  ) {
    return this.httpClient.post<{eventId: number}>(
      '/api/workout/trainingpeaks-sync/holds/resolve',
      {selection, revision, sourceWorkoutId, acknowledgeManualVerification: true},
      {headers: {Authorization: 'Bearer ' + token}}
    );
  }

  repairTrainingPeaksLink(
    selection: TrainingPeaksPreviewSelection, revision: string, sourceWorkoutId: string,
    oldEventId: number, replacementEventId: number | null, acknowledgeInconclusive: boolean, token: string
  ) {
    return this.httpClient.post<{eventId: number; recreated: boolean}>(
      '/api/workout/trainingpeaks-sync/links/repair',
      {selection, revision, sourceWorkoutId, oldEventId, replacementEventId, acknowledgeRepair: true, acknowledgeInconclusive},
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

  scheduleCopyCalendarToCalendar(types, skipSynced, platformDirection, window?: ScheduleWindow): Observable<any> {
    return this.httpClient
      .post(`/api/workout/copy-calendar-to-calendar/schedule`, {
        types,
        skipSynced,
        ...platformDirection,
        ...(window ? {window} : {})
      })
  }

  getScheduleSettings(): Observable<ScheduleSettings> {
    return this.httpClient.get<ScheduleSettings>('/api/workout/copy-calendar-to-calendar/schedule/settings');
  }

  getScheduleRequests(): Observable<ScheduledWorkoutRequest[]> {
    return this.httpClient.get<ScheduledWorkoutRequest[]>(`/api/workout/copy-calendar-to-calendar/schedule`)
  }

  deleteScheduleRequest(id: any) {
    return this.httpClient.delete(`/api/workout/copy-calendar-to-calendar/schedule/${id}`)
  }
}
