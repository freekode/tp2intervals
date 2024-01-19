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
export class TrainingPeaksActionsComponent {

  copyPlanFormGroup: FormGroup = this.formBuilder.group({
    startDate: [null, Validators.required],
    endDate: [null, Validators.required],
  });

  copyPlanInProgress = false
  planWorkoutInProgress = false

  constructor(
    private router: Router,
    private formBuilder: FormBuilder,
    private workoutService: WorkoutService,
    private notificationService: NotificationService
  ) {
  }

  copyPlanSubmit() {
    this.copyPlanInProgress = true
    let startDate = this.copyPlanFormGroup.value.startDate.toISOString().split('T')[0]
    let endDate = this.copyPlanFormGroup.value.endDate.toISOString().split('T')[0]
    this.workoutService.copyPlan(startDate, endDate).pipe(
      finalize(() => this.copyPlanInProgress = false)
    ).subscribe(() => {
      this.notificationService.success('Plan copied')
    })
  }

  planWorkoutClick() {
    this.planWorkoutInProgress = true
    this.workoutService.planWorkout().pipe(
      finalize(() => this.planWorkoutInProgress = false)
    ).subscribe(() => {
      this.notificationService.success('Workouts planned')
    })
  }
}
