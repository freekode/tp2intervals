import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from "@angular/forms";
import { formatDate } from "utils/date-formatter";
import { WorkoutClient } from "infrastructure/workout.client";
import { ConfigurationClient } from "infrastructure/configuration.client";
import { NotificationService } from "infrastructure/notification.service";
import { finalize } from "rxjs";
import { MatGridListModule } from "@angular/material/grid-list";
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
import { TpPlanWorkoutsComponent } from "app/training-peaks-actions/tp-plan-workouts/tp-plan-workouts.component";

@Component({
  selector: 'app-tp-copy-workouts',
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
    TpCopyPlanComponent,
    TpPlanWorkoutsComponent
  ],
  templateUrl: './tp-copy-workouts.component.html',
  styleUrl: './tp-copy-workouts.component.scss'
})
export class TpCopyWorkoutsComponent implements OnInit {

  formGroup: FormGroup = this.formBuilder.group({
    name: [null, Validators.required],
    trainingTypes: [null, Validators.required],
    startDate: [null, Validators.required],
    endDate: [null, Validators.required],
    isPlan: [null, Validators.required],
  });

  inProgress = false

  trainingTypes: any[];
  planType = [
    {name: 'Plan', value: true},
    {name: 'Folder', value: false}
  ]

  private readonly selectedTrainingTypes = ['BIKE', 'VIRTUAL_BIKE', 'MTB', 'RUN'];

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

  copyWorkoutsSubmit() {
    this.inProgress = true
    let name = this.formGroup.value.name
    let trainingTypes = this.formGroup.value.trainingTypes
    let startDate = formatDate(this.formGroup.value.startDate)
    let endDate = formatDate(this.formGroup.value.endDate)
    let direction = {sourcePlatform: 'TRAINING_PEAKS', targetPlatform: 'INTERVALS'}
    let isPlan = {sourcePlatform: 'TRAINING_PEAKS', targetPlatform: 'INTERVALS'}
    this.workoutClient.copyWorkouts(name, startDate, endDate, trainingTypes, direction, isPlan).pipe(
      finalize(() => this.inProgress = false)
    ).subscribe((response) => {
      this.notificationService.success(
        `Copied: ${response.copied}\n Filtered out: ${response.filteredOut}\n From ${response.startDate} to ${response.endDate}`)
    })
  }

  private initFormValues() {
    this.formGroup.patchValue({
      name: 'My New Plan',
      trainingTypes: this.selectedTrainingTypes,
      isPlan: true
    })
  }
}
