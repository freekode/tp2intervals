import { Component, OnInit } from '@angular/core';
import { MatGridListModule } from "@angular/material/grid-list";
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from "@angular/forms";
import { MatButtonModule } from "@angular/material/button";
import { MatCardModule } from "@angular/material/card";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatInputModule } from "@angular/material/input";
import { MatProgressBarModule } from "@angular/material/progress-bar";
import { formatDate, NgIf } from "@angular/common";
import { MatDatepickerModule } from "@angular/material/datepicker";
import { MatNativeDateModule } from "@angular/material/core";
import { MatSnackBarModule } from "@angular/material/snack-bar";
import { Router } from "@angular/router";
import { NotificationService } from "infrastructure/notification.service";
import { finalize } from "rxjs";
import { WorkoutClient } from "infrastructure/workout.client";
import { MatSelectModule } from "@angular/material/select";
import { ConfigurationClient } from "infrastructure/configuration.client";

const DATE_FORMAT = 'yyyy-MM-dd'
const LOCALE = 'en-US'

@Component({
  selector: 'app-training-peaks-actions',
  standalone: true,
  imports: [
    MatGridListModule,
    FormsModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    ReactiveFormsModule,
    MatProgressBarModule,
    NgIf,
    MatDatepickerModule,
    MatNativeDateModule,
    MatSnackBarModule,
    MatSelectModule
  ],
  templateUrl: './training-peaks-actions.component.html',
  styleUrl: './training-peaks-actions.component.scss'
})
export class TrainingPeaksActionsComponent implements OnInit {

  planWorkoutsFormGroup: FormGroup = this.formBuilder.group({
    direction: [null, Validators.required],
    trainingTypes: [null, Validators.required],
  });

  copyWorkoutsFormGroup: FormGroup = this.formBuilder.group({
    trainingTypes: [null, Validators.required],
    startDate: [null, Validators.required],
    endDate: [null, Validators.required],
  });

  jobStarted = true
  copyPlanInProgress = false
  planWorkoutInProgress = false

  directions = [
    {name: 'Intervals.icu -> Training Peaks', value: 'INTERVALS/TRAINING_PEAKS'},
    {name: 'Training Peaks -> Intervals.icu', value: 'TRAINING_PEAKS/INTERVALS'},
  ]
  trainingTypes: any[];

  private readonly selectedPlanDirection = this.directions[0].value;
  private readonly selectedTrainingTypes = ['BIKE', 'VIRTUAL_BIKE'];
  private readonly todayDate = formatDate(new Date(), DATE_FORMAT, LOCALE)
  private readonly tomorrowDate = formatDate(new Date(new Date().setDate(new Date().getDate() + 1)), DATE_FORMAT, LOCALE)

  constructor(
    private router: Router,
    private formBuilder: FormBuilder,
    private workoutClient: WorkoutClient,
    private configurationClient: ConfigurationClient,
    private notificationService: NotificationService
  ) {
  }

  ngOnInit(): void {
    this.planWorkoutsFormGroup.patchValue({
      direction: this.selectedPlanDirection
    })

    this.configurationClient.getTrainingTypes().subscribe(types => {
      this.trainingTypes = types
      this.planWorkoutsFormGroup.patchValue({
        trainingTypes: this.selectedTrainingTypes
      })
      this.copyWorkoutsFormGroup.patchValue({
        trainingTypes: this.selectedTrainingTypes
      })
    })
    // this.workoutClient.getJobPlanWorkout().subscribe(response => {
    //   this.jobStarted = !!response?.id
    // })
  }

  copyWorkoutsSubmit() {
    this.copyPlanInProgress = true
    let startDate = formatDate(this.copyWorkoutsFormGroup.value.startDate, DATE_FORMAT, LOCALE)
    let endDate = formatDate(this.copyWorkoutsFormGroup.value.endDate, DATE_FORMAT, LOCALE)
    let trainingTypes = this.planWorkoutsFormGroup.value.trainingTypes
    this.workoutClient.copyWorkouts(startDate, endDate, trainingTypes).pipe(
      finalize(() => this.copyPlanInProgress = false)
    ).subscribe((response) => {
      this.notificationService.success(
        `Copied ${response.copied} workout(s)\n Filtered out ${response.filteredOut} workout(s)\n From ${response.startDate} to ${response.endDate}`)
    })
  }

  planTodayClick() {
    this.planWorkouts(this.todayDate)
  }

  planTomorrowClick() {
    this.planWorkouts(this.tomorrowDate)
  }

  private planWorkouts(date) {
    this.planWorkoutInProgress = true
    let direction = this.planWorkoutsFormGroup.value.direction
    let trainingTypes = this.planWorkoutsFormGroup.value.trainingTypes
    this.workoutClient.planWorkout(direction, date, date, trainingTypes).pipe(
      finalize(() => this.planWorkoutInProgress = false)
    ).subscribe((response) => {
      this.notificationService.success(
        `Planned ${response.planned} workout(s)\n Filtered out ${response.filteredOut} workout(s)\n From ${response.startDate} to ${response.endDate}`)
    })
  }

  startJob() {
    this.workoutClient.startJobPlanWorkout().subscribe(() => {
      this.jobStarted = true
    })
  }

  stopJob() {
    this.workoutClient.stopJobPlanWorkout().subscribe(() => {
      this.jobStarted = false
    })
  }
}
