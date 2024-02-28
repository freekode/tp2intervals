import { Component, OnInit } from '@angular/core';
import { MatGridListModule } from "@angular/material/grid-list";
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from "@angular/forms";
import { MatButtonModule } from "@angular/material/button";
import { MatCardModule } from "@angular/material/card";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatInputModule } from "@angular/material/input";
import { MatProgressBarModule } from "@angular/material/progress-bar";
import { NgIf } from "@angular/common";
import { MatDatepickerModule } from "@angular/material/datepicker";
import { MatNativeDateModule } from "@angular/material/core";
import { MatSnackBarModule } from "@angular/material/snack-bar";
import { Router } from "@angular/router";
import { NotificationService } from "infrastructure/notification.service";
import { finalize } from "rxjs";
import { WorkoutClient } from "infrastructure/workout.client";
import { MatSelectModule } from "@angular/material/select";
import { ConfigurationClient } from "infrastructure/configuration.client";
import { formatDate } from "utils/date-formatter";
import { MatCheckboxModule } from "@angular/material/checkbox";

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
    MatSelectModule,
    MatCheckboxModule
  ],
  templateUrl: './training-peaks-actions.component.html',
  styleUrl: './training-peaks-actions.component.scss'
})
export class TrainingPeaksActionsComponent implements OnInit {

  planWorkoutsFormGroup: FormGroup = this.formBuilder.group({
    direction: [null, Validators.required],
    trainingTypes: [null, Validators.required],
    skipSynced: [null, Validators.required],
  });

  copyWorkoutsFormGroup: FormGroup = this.formBuilder.group({
    name: [null, Validators.required],
    trainingTypes: [null, Validators.required],
    startDate: [null, Validators.required],
    endDate: [null, Validators.required],
  });

  copyPlanInProgress = false
  planWorkoutInProgress = false

  directions = [
    {name: 'Intervals.icu -> Training Peaks', value: {sourcePlatform: 'INTERVALS', targetPlatform: 'TRAINING_PEAKS'}},
    {name: 'Training Peaks -> Intervals.icu', value: {sourcePlatform: 'TRAINING_PEAKS', targetPlatform: 'INTERVALS'}},
  ]
  trainingTypes: any[];

  private readonly selectedPlanDirection = this.directions[0].value;
  private readonly selectedTrainingTypes = ['BIKE', 'VIRTUAL_BIKE', 'MTB', 'RUN'];
  private readonly todayDate = formatDate(new Date())
  private readonly tomorrowDate = formatDate(new Date(new Date().setDate(new Date().getDate() + 1)))

  constructor(
    private router: Router,
    private formBuilder: FormBuilder,
    private workoutClient: WorkoutClient,
    private configurationClient: ConfigurationClient,
    private notificationService: NotificationService
  ) {
  }

  ngOnInit(): void {
    this.configurationClient.getTrainingTypes().subscribe(types => {
      this.trainingTypes = types
      this.initFormValues();
    })
  }

  planTodayClick() {
    this.planWorkouts(this.todayDate, this.todayDate)
  }

  planTomorrowClick() {
    this.planWorkouts(this.tomorrowDate, this.tomorrowDate)
  }

  copyWorkoutsSubmit() {
    this.copyPlanInProgress = true
    let name = this.copyWorkoutsFormGroup.value.name
    let trainingTypes = this.planWorkoutsFormGroup.value.trainingTypes
    let startDate = formatDate(this.copyWorkoutsFormGroup.value.startDate)
    let endDate = formatDate(this.copyWorkoutsFormGroup.value.endDate)
    let direction = {sourcePlatform: 'TRAINING_PEAKS', targetPlatform: 'INTERVALS'}
    this.workoutClient.copyWorkouts(name, startDate, endDate, trainingTypes, direction).pipe(
      finalize(() => this.copyPlanInProgress = false)
    ).subscribe((response) => {
      this.notificationService.success(
        `Copied: ${response.copied}\n Filtered out: ${response.filteredOut}\n From ${response.startDate} to ${response.endDate}`)
    })
  }

  private planWorkouts(startDate, endDate) {
    this.planWorkoutInProgress = true
    let skipSynced = this.planWorkoutsFormGroup.value.skipSynced
    let direction = this.planWorkoutsFormGroup.value.direction
    let trainingTypes = this.planWorkoutsFormGroup.value.trainingTypes
    this.workoutClient.planWorkout(startDate, endDate, trainingTypes, skipSynced, direction).pipe(
      finalize(() => this.planWorkoutInProgress = false)
    ).subscribe((response) => {
      this.notificationService.success(
        `Planned: ${response.planned}\n Filtered out: ${response.filteredOut}\n From ${response.startDate} to ${response.endDate}`)
    })
  }

  private initFormValues() {
    this.planWorkoutsFormGroup.patchValue({
      direction: this.selectedPlanDirection,
      trainingTypes: this.selectedTrainingTypes,
      skipSynced: true
    })
    this.copyWorkoutsFormGroup.patchValue({
      name: 'My New Plan',
      trainingTypes: this.selectedTrainingTypes
    })
  }
}
