import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogActions, MatDialogClose, MatDialogContent, MatDialogTitle} from '@angular/material/dialog';
import {MatButtonModule} from '@angular/material/button';

export interface SyncPreview {
  startDate: string;
  endDate: string;
  copied: number;
  updated: number;
  removed: number;
  alreadySynced: number;
  skipped: number;
  filteredOut: number;
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
      <p>Add: {{data.copied}} | Update: {{data.updated}} | Remove: {{data.removed}}</p>
      <p>Already synced: {{data.alreadySynced}} | Skipped (ambiguous or protected): {{data.skipped}} | Filtered by type: {{data.filteredOut}}</p>
      <ul>
        @for (action of data.previewActions; track $index) {
          <li>{{action.action}}: {{action.date}} - {{action.name}} (TrainerRoad ID {{action.trainerRoadId}})</li>
        }
      </ul>
      <p>If the list of affected workouts changes before confirmation, sync stops and you can preview again.</p>
      <p>If sync fails, some earlier changes may already have succeeded.</p>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button [mat-dialog-close]="false" cdkFocusInitial>Cancel</button>
      <button mat-raised-button color="primary" [mat-dialog-close]="true">Sync</button>
    </mat-dialog-actions>
  `
})
export class SyncPreviewDialogComponent {
  constructor(@Inject(MAT_DIALOG_DATA) public data: SyncPreview) {}
}
