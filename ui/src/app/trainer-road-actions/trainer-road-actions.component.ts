import { Component } from '@angular/core';
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
import { WorkoutService } from "infrastructure/workout.service";
import { NotificationService } from "infrastructure/notification.service";
import { finalize } from "rxjs";
import { WorkoutClient } from "infrastructure/workout.client";

@Component({
  selector: 'app-trainer-road-actions',
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
  templateUrl: './trainer-road-actions.component.html',
  styleUrl: './trainer-road-actions.component.scss'
})
export class TrainerRoadActionsComponent {
  syncWorkoutsFormGroup: FormGroup = this.formBuilder.group({
    startDate: [null, Validators.required],
    endDate: [null, Validators.required],
  });

  jobStarted = true
  copyPlanInProgress = false

  constructor(
    private router: Router,
    private formBuilder: FormBuilder,
    private workoutService: WorkoutService,
    private workoutClient: WorkoutClient,
    private notificationService: NotificationService
  ) {
  }

  ngOnInit(): void {
    this.workoutClient.getJobPlanWorkout().subscribe(response => {
      this.jobStarted = !!response?.id
    })
  }

  syncWorkoutsSubmit() {
    this.copyPlanInProgress = true
    let startDate = this.syncWorkoutsFormGroup.value.startDate.toISOString().split('T')[0]
    let endDate = this.syncWorkoutsFormGroup.value.endDate.toISOString().split('T')[0]
    this.workoutService.copyPlan(startDate, endDate).pipe(
      finalize(() => this.copyPlanInProgress = false)
    ).subscribe(() => {
      this.notificationService.success('Workouts are synced')
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
