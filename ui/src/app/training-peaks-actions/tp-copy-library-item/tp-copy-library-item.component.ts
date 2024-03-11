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
import { WorkoutClient } from "infrastructure/workout.client";
import { ConfigurationClient } from "infrastructure/configuration.client";
import { NotificationService } from "infrastructure/notification.service";
import { finalize } from "rxjs";
import { LibraryClient } from "infrastructure/library-client.service";
import { Platform } from "infrastructure/platform";

@Component({
  selector: 'tp-copy-library-item',
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
  templateUrl: './tp-copy-library-item.component.html',
  styleUrl: './tp-copy-library-item.component.scss'
})
export class TpCopyLibraryItemComponent implements OnInit {

  formGroup: FormGroup = this.formBuilder.group({
    plan: [null, Validators.required],
    newName: [null, Validators.required],
  });

  inProgress = false

  plans: any[];

  constructor(
    private formBuilder: FormBuilder,
    private workoutClient: WorkoutClient,
    private planClient: LibraryClient,
    private configurationClient: ConfigurationClient,
    private notificationService: NotificationService
  ) {
  }

  ngOnInit(): void {
    this.formGroup.disable()
    this.planClient.getLibraries(Platform.TRAINING_PEAKS.key).subscribe(plans => {
      this.plans = plans.map(plan => {
        return {name: plan.name + (plan.isPlan ? ' (plan)' : ''), value: plan}
      })
      this.formGroup.enable()
    })
    this.formGroup.controls['plan'].valueChanges.subscribe(value => {
      this.formGroup.patchValue({
        newName: value.name
      })
    })
  }

  copyPlanSubmit() {
    this.inProgress = true
    let plan = this.formGroup.value.plan
    let newName = this.formGroup.value.newName
    let direction = {sourcePlatform: 'TRAINING_PEAKS', targetPlatform: 'INTERVALS'}
    this.planClient.copyLibrary(plan, newName, direction).pipe(
      finalize(() => this.inProgress = false)
    ).subscribe((response) => {
      this.notificationService.success(
        `Library name: ${response.planName}\nCopied workouts: ${response.workouts}`)
    })
  }
}
