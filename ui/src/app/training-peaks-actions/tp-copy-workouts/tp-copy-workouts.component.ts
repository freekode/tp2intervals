import { Component, OnInit } from '@angular/core';
import { MatButtonModule } from "@angular/material/button";
import { MatCardModule } from "@angular/material/card";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatOptionModule } from "@angular/material/core";
import { MatProgressBarModule } from "@angular/material/progress-bar";
import { MatSelectModule } from "@angular/material/select";
import { AsyncPipe, NgIf } from "@angular/common";
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from "@angular/forms";
import { WorkoutClient } from "infrastructure/workout.client";
import { PlanClient } from "infrastructure/plan.client";
import { ConfigurationClient } from "infrastructure/configuration.client";
import { NotificationService } from "infrastructure/notification.service";
import { Platform } from "infrastructure/platform";
import { debounceTime, filter, finalize, Observable, switchMap, tap } from "rxjs";
import { MatInputModule } from "@angular/material/input";
import { MatAutocompleteModule } from "@angular/material/autocomplete";
import { MatProgressSpinnerModule } from "@angular/material/progress-spinner";

@Component({
  selector: 'tp-copy-workouts',
  standalone: true,
  imports: [
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatOptionModule,
    MatProgressBarModule,
    MatSelectModule,
    NgIf,
    ReactiveFormsModule,
    MatInputModule,
    MatAutocompleteModule,
    AsyncPipe,
    MatProgressSpinnerModule
  ],
  templateUrl: './tp-copy-workouts.component.html',
  styleUrl: './tp-copy-workouts.component.scss'
})
export class TpCopyWorkoutsComponent implements OnInit {

  formGroup: FormGroup = this.formBuilder.group({
    tpWorkout: [null, Validators.required],
    intervalsPlan: [null, Validators.required],
  });

  searchInProgress = false
  inProgress = false

  workouts: Observable<any[]>;
  plans: any[];

  constructor(
    private formBuilder: FormBuilder,
    private workoutClient: WorkoutClient,
    private planClient: PlanClient,
    private configurationClient: ConfigurationClient,
    private notificationService: NotificationService
  ) {
  }

  ngOnInit(): void {
    this.loadPlans();
    this.subscribeOnWorkoutChange();
  }

  submit() {
    this.inProgress = true
    let plan = this.formGroup.value.plan
    let direction = {sourcePlatform: 'TRAINING_PEAKS', targetPlatform: 'INTERVALS'}
    this.planClient.copyPlan(plan, direction).pipe(
      finalize(() => this.inProgress = false)
    ).subscribe((response) => {
      this.notificationService.success(
        `Plan name: ${response.planName}\nCopied workouts: ${response.workouts}`)
    })
  }

  displayFn(workout): string {
    return workout ? workout.name : '';
  }

  private loadPlans() {
    this.formGroup.disable()
    this.planClient.getPlans(Platform.INTERVALS.key).subscribe(plans => {
      this.plans = plans.map(plan => {
        return {name: plan.name, value: plan}
      })
      this.formGroup.enable()
    })
  }

  private subscribeOnWorkoutChange() {
    this.workouts = this.formGroup.controls['tpWorkout'].valueChanges.pipe(
      debounceTime(300),
      filter(value => !!value && value.length > 2),
      tap(() => {
        this.searchInProgress = true
      }),
      switchMap(value => this.workoutClient.findWorkoutsByName(Platform.TRAINING_PEAKS.key, value).pipe(
        finalize(() => {
          this.searchInProgress = false
        })
      ))
    )
  }
}
