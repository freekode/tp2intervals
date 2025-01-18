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
import {MatDividerModule} from "@angular/material/divider";
import {MatListModule} from "@angular/material/list";
import {
  CopyCalendarToCalendarComponent
} from "app/components/copy-calendar-to-calendar/copy-calendar-to-calendar.component";

@Component({
  selector: 'tr-copy-calendar-to-calendar',
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
    MatDividerModule,
    MatListModule,
    CopyCalendarToCalendarComponent,
  ],
  templateUrl: './tr-copy-calendar-to-calendar.component.html',
  styleUrl: './tr-copy-calendar-to-calendar.component.scss'
})
export class TrCopyCalendarToCalendarComponent implements OnInit {
  readonly Platform = Platform;
  readonly directions = [
    {title: "TrainerRoad -> TrainingPeaks", value: Platform.DIRECTION_TR_TP},
    {title: "TrainerRoad -> Intervals.icu", value: Platform.DIRECTION_TR_INT},
  ]
  readonly trainingTypes = [
    {title: "Ride", value: "BIKE"},
    {title: "Virtual Ride", value: "VIRTUAL_BIKE"},
    {title: "Unknown", value: "UNKNOWN"},
  ];

  constructor() {
  }

  ngOnInit(): void {
  }
}
