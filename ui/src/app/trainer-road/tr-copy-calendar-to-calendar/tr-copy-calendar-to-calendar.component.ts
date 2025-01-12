import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators} from "@angular/forms";
import {MatButtonModule} from "@angular/material/button";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatInputModule} from "@angular/material/input";
import {MatProgressBarModule} from "@angular/material/progress-bar";
import {NgIf} from "@angular/common";
import {MatDatepickerModule} from "@angular/material/datepicker";
import {MatNativeDateModule} from "@angular/material/core";
import {MatSnackBarModule} from "@angular/material/snack-bar";
import {MatSelectModule} from "@angular/material/select";
import {MatCheckboxModule} from "@angular/material/checkbox";
import {WorkoutClient} from "infrastructure/client/workout.client";
import {NotificationService} from "infrastructure/notification.service";
import {finalize} from "rxjs";
import {Platform} from "infrastructure/platform";
import {formatDate} from "utils/date-formatter";
import {TrainingPeaksTrainingTypes} from "app/training-peaks/training-peaks-training-types";
import {TrainerRoadTrainingTypes} from "app/trainer-road/trainer-road-training-types";

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
    NgIf,
    MatDatepickerModule,
    MatNativeDateModule,
    MatSnackBarModule,
    MatSelectModule,
    MatCheckboxModule,
  ],
  templateUrl: './tr-copy-calendar-to-calendar.component.html',
  styleUrl: './tr-copy-calendar-to-calendar.component.scss'
})
export class TrCopyCalendarToCalendarComponent implements OnInit {
  readonly todayDate = new Date()
  readonly tomorrowDate = new Date(new Date().getTime() + 24 * 60 * 60 * 1000)
  readonly directions = [
    {title: "TrainerRoad -> TrainingPeaks", value: Platform.DIRECTION_TR_TP},
    {title: "TrainerRoad -> Intervals.icu", value: Platform.DIRECTION_TR_INT},
  ]
  readonly selectedTrainingTypes = ['BIKE', 'VIRTUAL_BIKE'];

  formGroup: FormGroup = this.formBuilder.group({
    direction: [this.directions[0].value, Validators.required],
    trainingTypes: [this.selectedTrainingTypes, Validators.required],
    startDate: [this.todayDate, Validators.required],
    endDate: [this.tomorrowDate, Validators.required],
    skipSynced: [true, Validators.required],
  });

  trainingTypes = TrainerRoadTrainingTypes.trainingTypes;
  maxDate: Date | null = null

  inProgress = false

  constructor(
    private formBuilder: FormBuilder,
    private workoutClient: WorkoutClient,
    private notificationService: NotificationService
  ) {
  }

  ngOnInit(): void {
    this.listenDirectionChange();
  }

  submit() {
    let startDate = formatDate(this.formGroup.controls['startDate'].value)
    let endDate = formatDate(this.formGroup.controls['endDate'].value)
    this.copyWorkouts(startDate, endDate);
  }

  today() {
    this.copyWorkoutsForOneDay(formatDate(this.todayDate));
  }

  tomorrow() {
    this.copyWorkoutsForOneDay(formatDate(this.tomorrowDate));
  }

  private copyWorkoutsForOneDay(date) {
    this.copyWorkouts(date, date)
  }

  private copyWorkouts(startDate, endDate) {
    this.inProgress = true
    let direction = this.formGroup.value.direction
    let trainingTypes = this.formGroup.value.trainingTypes
    let skipSynced = this.formGroup.value.skipSynced
    this.workoutClient.copyCalendarToCalendar(startDate, endDate, trainingTypes, skipSynced, direction).pipe(
      finalize(() => this.inProgress = false)
    ).subscribe((response) => {
      this.notificationService.success(
        `Planned: ${response.copied}\n Filtered out: ${response.filteredOut}\n From ${response.startDate} to ${response.endDate}`)
    })
  }

  private listenDirectionChange() {
    this.formGroup.controls['direction'].valueChanges.subscribe(value => {
      if (value === Platform.DIRECTION_TR_INT) {
        this.maxDate = null
        this.formGroup.controls['skipSynced'].disable()
        this.formGroup.controls['skipSynced'].setValue(false)
      } else {
        this.maxDate = this.tomorrowDate
        this.formGroup.controls['skipSynced'].enable()
        this.formGroup.controls['skipSynced'].setValue(true)
      }
    })
  }
}
