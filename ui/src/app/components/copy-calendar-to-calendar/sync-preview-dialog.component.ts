import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogActions, MatDialogClose, MatDialogContent, MatDialogTitle} from '@angular/material/dialog';
import {MatButtonModule} from '@angular/material/button';

export interface SkippedSyncWorkout {
  date: string;
  name: string;
  trainerRoadId: string;
  intervalsIds: string[];
  reason: 'DUPLICATE_TRAINERROAD_ID' | 'DUPLICATE_INTERVALS_ID' | 'PROTECTED_EVENT' | 'UNIDENTIFIED_ACTIVITY';
}

export function describeSkippedWorkout(workout: SkippedSyncWorkout): string {
  const reasons: Record<SkippedSyncWorkout['reason'], string> = {
    DUPLICATE_TRAINERROAD_ID: 'duplicate TrainerRoad ID on this day',
    DUPLICATE_INTERVALS_ID: 'multiple Intervals events match this ID',
    PROTECTED_EVENT: 'Intervals event does not have a clear TrainerRoad ID',
    UNIDENTIFIED_ACTIVITY: 'stale removal was paused due to an activity without an ID (older policy)',
  };
  const intervals = workout.intervalsIds.length ? `; Intervals ID: ${workout.intervalsIds.join(', ')}` : '';
  return `${workout.date}: ${workout.name} (TrainerRoad ID: ${workout.trainerRoadId}${intervals}) - ${reasons[workout.reason]}`;
}

export interface SyncPreview {
  trainerRoadSyncMode: 'ADD_MISSING' | 'MIRROR';
  startDate: string;
  endDate: string;
  copied: number;
  updated: number;
  removed: number;
  alreadySynced: number;
  skipped: number;
  filteredOut: number;
  unidentifiedActivityDates?: string[];
  skippedWorkouts?: SkippedSyncWorkout[];
  previewActions: Array<{action: string, date: string, name: string, trainerRoadId: string, intervalsId: string | null}>;
}

@Component({
  selector: 'sync-preview-dialog',
  standalone: true,
  imports: [MatDialogTitle, MatDialogContent, MatDialogActions, MatDialogClose, MatButtonModule],
  template: `
    <h2 mat-dialog-title>Preview calendar sync</h2>
    <mat-dialog-content>
      <p>{{data.startDate}} to {{data.endDate}}</p>
      @if (hasChanges) {
        <p>Add: {{data.copied}} | Update: {{data.updated}} | Remove: {{data.removed}}</p>
        <ul>
          @for (action of data.previewActions; track $index) {
            <li>{{action.action}}: {{action.date}} - {{action.name}} (TrainerRoad ID {{action.trainerRoadId}}@if (action.intervalsId) {; Intervals ID {{action.intervalsId}}})</li>
          }
        </ul>
      } @else {
        <p><strong>No changes planned.</strong></p>
      }
      @if (data.alreadySynced) {
        <p>{{data.trainerRoadSyncMode === 'ADD_MISSING' ? 'Existing matches (not checked for changes)' : 'Unchanged matches'}}: {{data.alreadySynced}}</p>
      }
      @if (data.skipped) {
        <p><strong>{{data.skipped}} {{data.skipped === 1 ? 'entry was' : 'entries were'}} skipped due to ambiguity or protection.</strong> Please check manually.</p>
        @if (data.skippedWorkouts?.length) {
          <ul>
            @for (workout of data.skippedWorkouts; track $index) {
              <li>{{describeSkippedWorkout(workout)}}</li>
            }
          </ul>
        }
      }
      @if (data.unidentifiedActivityDates?.length) {
        <p><strong>TrainerRoad activities without workout IDs were found on: {{data.unidentifiedActivityDates?.join(', ')}}.</strong> Please check manually.@if (data.trainerRoadSyncMode === 'MIRROR') { Marked Intervals events may be removed even if they correspond to these activities.}</p>
      }
      @if (data.filteredOut) {
        <p>TrainerRoad workouts filtered out by type: {{data.filteredOut}}</p>
      }
      @if (hasChanges) {
        <p>If the list of actions or, in Mirror mode, the dates requiring manual review change before confirmation, sync stops and you can preview again. This check does not compare every workout detail.</p>
        <p>If sync fails, some earlier changes may already have succeeded.</p>
      }
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button [mat-dialog-close]="false" cdkFocusInitial>{{hasChanges ? 'Cancel' : 'Close'}}</button>
      @if (hasChanges) {
        <button mat-raised-button color="primary" [mat-dialog-close]="true">Sync workouts</button>
      }
    </mat-dialog-actions>
  `
})
export class SyncPreviewDialogComponent {
  readonly describeSkippedWorkout = describeSkippedWorkout;
  constructor(@Inject(MAT_DIALOG_DATA) public data: SyncPreview) {}

  get hasChanges(): boolean {
    return this.data.copied + this.data.updated + this.data.removed > 0;
  }
}
