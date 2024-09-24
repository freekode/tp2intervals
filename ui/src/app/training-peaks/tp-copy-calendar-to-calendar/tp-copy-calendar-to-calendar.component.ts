import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from "@angular/forms";
import { MatButtonModule } from "@angular/material/button";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatInputModule } from "@angular/material/input";
import { MatProgressBarModule } from "@angular/material/progress-bar";
import { NgIf } from "@angular/common";
import { MatDatepickerModule } from "@angular/material/datepicker";
import { MatNativeDateModule } from "@angular/material/core";
import { MatSnackBarModule } from "@angular/material/snack-bar";
import { MatSelectModule } from "@angular/material/select";
import { MatCheckboxModule } from "@angular/material/checkbox";
import { WorkoutClient } from "infrastructure/client/workout.client";
import { ConfigurationClient } from "infrastructure/client/configuration.client";
import { NotificationService } from "infrastructure/notification.service";
import { finalize } from "rxjs";
import { Platform } from "infrastructure/platform";
import { formatDate } from "utils/date-formatter";

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
    NgIf,
    MatDatepickerModule,
    MatNativeDateModule,
    MatSnackBarModule,
    MatSelectModule,
    MatCheckboxModule,
  ],
  templateUrl: './tp-copy-calendar-to-calendar.component.html',
  styleUrl: './tp-copy-calendar-to-calendar.component.scss'
})
export class TpCopyCalendarToCalendarComponent implements OnInit {
  private readonly todayDate = new Date()
  readonly tomorrowDate = new Date(new Date().getTime() + 24 * 60 * 60 * 1000)
  private readonly selectedTrainingTypes = ['BIKE', 'VIRTUAL_BIKE', 'MTB', 'RUN'];
  private readonly direction = Platform.DIRECTION_INT_TP

  formGroup: FormGroup = this.formBuilder.group({
    trainingTypes: [this.selectedTrainingTypes, Validators.required],
    startDate: [this.todayDate, Validators.required],
    endDate: [this.tomorrowDate, Validators.required],
    skipSynced: [true, Validators.required],
  });

  trainingTypes: any[];

  inProgress = false

  constructor(
    private formBuilder: FormBuilder,
    private workoutClient: WorkoutClient,
    private configurationClient: ConfigurationClient,
    private notificationService: NotificationService
  ) {
  }

  ngOnInit(): void {
    this.configurationClient.getTrainingTypes().subscribe(types => {
      this.trainingTypes = types
    })
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
    let trainingTypes = this.formGroup.value.trainingTypes
    let skipSynced = this.formGroup.value.skipSynced
    this.workoutClient.copyCalendarToCalendar(startDate, endDate, trainingTypes, skipSynced, this.direction).pipe(
      finalize(() => this.inProgress = false)
    ).subscribe((response) => {
      this.notificationService.success(
        `Planned: ${response.copied}\n Filtered out: ${response.filteredOut}\n From ${response.startDate} to ${response.endDate}`)
    })
  }
}
