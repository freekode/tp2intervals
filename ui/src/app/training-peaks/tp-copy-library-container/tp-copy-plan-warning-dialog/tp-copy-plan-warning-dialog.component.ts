import { Component, Inject } from '@angular/core';
import {
  MAT_DIALOG_DATA,
  MatDialogActions,
  MatDialogClose,
  MatDialogContent,
  MatDialogRef, MatDialogTitle
} from "@angular/material/dialog";
import { MatButtonModule } from "@angular/material/button";

@Component({
  selector: 'app-tp-copy-plan-warning-dialog',
  standalone: true,
  imports: [
    MatDialogContent,
    MatDialogActions,
    MatDialogClose,
    MatButtonModule,
    MatDialogTitle
  ],
  templateUrl: './tp-copy-plan-warning-dialog.component.html',
  styleUrl: './tp-copy-plan-warning-dialog.component.scss'
})
export class TpCopyPlanWarningDialogComponent {
  constructor(
    public dialogRef: MatDialogRef<TpCopyPlanWarningDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
  ) {}
}
