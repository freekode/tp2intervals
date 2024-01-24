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
    MatSnackBarModule
  ],
  templateUrl: './training-peaks-actions.component.html',
  styleUrl: './training-peaks-actions.component.scss'
})
export class TrainingPeaksActionsComponent implements OnInit {

  copyPlanFormGroup: FormGroup = this.formBuilder.group({
    startDate: [null, Validators.required],
    endDate: [null, Validators.required],
  });

  jobStarted = true
  copyPlanInProgress = false
  planWorkoutInProgress = false

  constructor(
    private router: Router,
    private formBuilder: FormBuilder,
    private workoutClient: WorkoutClient,
    private notificationService: NotificationService
  ) {
  }

  ngOnInit(): void {
    this.workoutClient.getJobPlanWorkout().subscribe(response => {
      this.jobStarted = !!response?.id
    })
  }

  copyPlanSubmit() {
    this.copyPlanInProgress = true
    let startDate = this.copyPlanFormGroup.value.startDate.toISOString().split('T')[0]
    let endDate = this.copyPlanFormGroup.value.endDate.toISOString().split('T')[0]
    this.workoutClient.copyPlan(startDate, endDate).pipe(
      finalize(() => this.copyPlanInProgress = false)
    ).subscribe(() => {
      this.notificationService.success('Plan copied')
    })
  }

  planWorkoutClick() {
    this.planWorkoutInProgress = true
    this.workoutClient.planWorkout().pipe(
      finalize(() => this.planWorkoutInProgress = false)
    ).subscribe(() => {
      this.notificationService.success('Workouts planned')
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
