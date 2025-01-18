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
  CopyCalendarToCalendarComponent
} from "app/components/copy-calendar-to-calendar/copy-calendar-to-calendar.component";

@Component({
  selector: 'tp-copy-calendar-to-calendar',
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
    CopyCalendarToCalendarComponent,
  ],
  templateUrl: './tp-copy-calendar-to-calendar.component.html',
  styleUrl: './tp-copy-calendar-to-calendar.component.scss'
})
export class TpCopyCalendarToCalendarComponent implements OnInit {
  readonly Platform = Platform;
  readonly directions = [
    {title: "Intervals.icu -> TrainingPeaks", value: Platform.DIRECTION_INT_TP},
    {title: "TrainingPeaks -> Intervals.icu", value: Platform.DIRECTION_TP_INT},
  ]
  readonly trainingTypes = [
    {title: "Ride", value: "BIKE"},
    {title: "MTB", value: "MTB"},
    {title: "Virtual Ride", value: "VIRTUAL_BIKE"},
    {title: "Run", value: "RUN"},
    {title: "Swim", value: "SWIM"},
    {title: "Walk", value: "WALK"},
    {title: "Weight Training", value: "WEIGHT"},
    {title: "Any other", value: "UNKNOWN"},
  ]
  readonly selectedTrainingTypes = ['BIKE', 'VIRTUAL_BIKE', 'MTB', 'RUN'];

  constructor() {
  }

  ngOnInit(): void {
  }
}
