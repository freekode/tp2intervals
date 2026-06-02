import {Component, OnInit} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {MatButtonModule} from "@angular/material/button";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatInputModule} from "@angular/material/input";
import {MatProgressBarModule} from "@angular/material/progress-bar";
import {MatDatepickerModule} from "@angular/material/datepicker";
import {MatNativeDateModule} from "@angular/material/core";
import {MatSnackBarModule} from "@angular/material/snack-bar";
import {MatSelectModule} from "@angular/material/select";
import {MatCheckboxModule} from "@angular/material/checkbox";
import {Platform} from "infrastructure/platform";
import {MatListModule} from "@angular/material/list";
import {
  CopyWellnessToCalendarComponent
} from "app/components/copy-wellness-to-calendar/copy-wellness-to-calendar.component";

@Component({
  selector: 'tp-copy-wellness-to-calendar',
  standalone: true,
  imports: [
    FormsModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    ReactiveFormsModule,
    MatProgressBarModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatSnackBarModule,
    MatSelectModule,
    MatCheckboxModule,
    MatListModule,
    CopyWellnessToCalendarComponent,
  ],
  templateUrl: './tp-copy-wellness-to-calendar.component.html',
  styleUrl: './tp-copy-wellness-to-calendar.component.scss'
})
export class TpCopyWellnessToCalendarComponent implements OnInit {
  readonly Platform = Platform;
  readonly directions = [
    {title: "Intervals.icu -> TrainingPeaks", value: Platform.DIRECTION_INT_TP},
    {title: "TrainingPeaks -> Intervals.icu", value: Platform.DIRECTION_TP_INT},
  ]
  readonly trainingTypes = [
    {title: "Weight", value: "WEIGHT"},
  ]
  readonly selectedTrainingTypes = ['WEIGHT'];

  constructor() {
  }

  ngOnInit(): void {
  }
}
