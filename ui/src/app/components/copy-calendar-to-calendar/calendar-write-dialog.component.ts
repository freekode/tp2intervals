import {Component, inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogModule} from '@angular/material/dialog';
import {MatButtonModule} from '@angular/material/button';

export interface CalendarWriteConfirmation {
  action: string;
  destination: string;
  details: string;
}

@Component({
  standalone: true,
  imports: [MatDialogModule, MatButtonModule],
  template: `
    <h2 mat-dialog-title>{{ data.action }}</h2>
    <mat-dialog-content>
      <p><strong>Destination:</strong> {{ data.destination }}</p>
      <p>{{ data.details }}</p>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button type="button" [mat-dialog-close]="false" cdkFocusInitial>Cancel</button>
      <button mat-raised-button color="primary" type="button" [mat-dialog-close]="true">{{ data.action }}</button>
    </mat-dialog-actions>
  `,
})
export class CalendarWriteDialogComponent {
  readonly data = inject<CalendarWriteConfirmation>(MAT_DIALOG_DATA);
}
