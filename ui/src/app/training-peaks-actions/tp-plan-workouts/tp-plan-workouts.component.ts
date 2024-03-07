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
import { MatSelectModule } from "@angular/material/select";
import { MatCheckboxModule } from "@angular/material/checkbox";
import { TpCopyPlanComponent } from "app/training-peaks-actions/tp-copy-plan/tp-copy-plan.component";
import { formatDate } from "utils/date-formatter";
import { WorkoutClient } from "infrastructure/workout.client";
import { ConfigurationClient } from "infrastructure/configuration.client";
import { NotificationService } from "infrastructure/notification.service";
import { finalize } from "rxjs";

@Component({
  selector: 'app-tp-plan-workouts',
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
    MatCheckboxModule,
    TpCopyPlanComponent
  ],
  templateUrl: './tp-plan-workouts.component.html',
  styleUrl: './tp-plan-workouts.component.scss'
})
export class TpPlanWorkoutsComponent  implements OnInit {

  formGroup: FormGroup = this.formBuilder.group({
    direction: [null, Validators.required],
    trainingTypes: [null, Validators.required],
    skipSynced: [null, Validators.required],
  });

  inProgress = false

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

  private planWorkouts(startDate, endDate) {
    this.inProgress = true
    let skipSynced = this.formGroup.value.skipSynced
    let direction = this.formGroup.value.direction
    let trainingTypes = this.formGroup.value.trainingTypes
    this.workoutClient.planWorkout(startDate, endDate, trainingTypes, skipSynced, direction).pipe(
      finalize(() => this.inProgress = false)
    ).subscribe((response) => {
      this.notificationService.success(
        `Planned: ${response.planned}\n Filtered out: ${response.filteredOut}\n From ${response.startDate} to ${response.endDate}`)
    })
  }

  private initFormValues() {
    this.formGroup.patchValue({
      direction: this.selectedPlanDirection,
      trainingTypes: this.selectedTrainingTypes,
      skipSynced: true
    })
  }
}