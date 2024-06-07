import { Component, OnInit } from '@angular/core';
import { MatGridListModule } from "@angular/material/grid-list";
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from "@angular/forms";
import { MatButtonModule } from "@angular/material/button";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatInputModule } from "@angular/material/input";
import { MatProgressBarModule } from "@angular/material/progress-bar";
import { AsyncPipe, NgIf } from "@angular/common";
import { MatDatepickerModule } from "@angular/material/datepicker";
import { MatNativeDateModule } from "@angular/material/core";
import { MatSnackBarModule } from "@angular/material/snack-bar";
import { MatSelectModule } from "@angular/material/select";
import { MatCheckboxModule } from "@angular/material/checkbox";
import { WorkoutClient } from "infrastructure/client/workout.client";
import { ConfigurationClient } from "infrastructure/client/configuration.client";
import { NotificationService } from "infrastructure/notification.service";
import { filter, finalize, map, of } from "rxjs";
import { LibraryClient } from "infrastructure/client/library-client.service";
import { Platform } from "infrastructure/platform";
import { MatDialog } from "@angular/material/dialog";
import {
  TpCopyPlanWarningDialogComponent
} from "app/training-peaks/tp-copy-library-container/tp-copy-plan-warning-dialog/tp-copy-plan-warning-dialog.component";
import { formatDate } from "utils/date-formatter";
import { ConfigData } from "infrastructure/config-data";
import { MatTooltip, MatTooltipModule } from "@angular/material/tooltip";

@Component({
  selector: 'tp-copy-library-container',
  standalone: true,
  imports: [
    MatGridListModule,
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
    AsyncPipe,
    MatTooltipModule
  ],
  templateUrl: './tp-copy-library-container.component.html',
  styleUrl: './tp-copy-library-container.component.scss'
})
export class TpCopyLibraryContainerComponent implements OnInit {
  private readonly mondayDate = formatDate(this.getMonday(new Date()))

  formGroup: FormGroup = this.formBuilder.group({
    plan: [null, Validators.required],
    newName: [null, Validators.required],
    newStartDate: [this.mondayDate, Validators.required],
    stepModifier: ['NONE', Validators.required],
  });

  // isPlanSelected = this.formGroup.controls['plan'].valueChanges.pipe(map(value => value?.isPlan))
  isPlanSelected = of(false)
  submitInProgress = false
  loadingInProgress = false

  plans: any[];
  stepModifiers: any[];
  config: any;

  constructor(
    private formBuilder: FormBuilder,
    public dialog: MatDialog,
    private workoutClient: WorkoutClient,
    private planClient: LibraryClient,
    private configurationClient: ConfigurationClient,
    private notificationService: NotificationService,
  ) {
  }

  ngOnInit(): void {
    this.formGroup.disable()
    this.loadingInProgress = true
    this.getConfig();
    this.getStepModifiers();
    this.getPlans();
    this.onPlanChange();
  }

  copyPlanSubmit() {
    let plan = this.formGroup.value.plan
    if (plan.workoutsAmount > 100) {
      this.openWarningDialog(plan, this.copyPlan)
      return
    }
    this.copyPlan()
  }

  private copyPlan() {
    this.submitInProgress = true
    let plan = this.formGroup.value.plan
    let newName = this.formGroup.value.newName
    let newStartDate = this.formGroup.value.newStartDate
    let stepModifier = this.formGroup.value.stepModifier
    let direction = Platform.DIRECTION_TP_INT
    this.planClient.copyLibraryContainer(plan, newName, newStartDate, stepModifier, direction).pipe(
      finalize(() => this.submitInProgress = false)
    ).subscribe((response) => {
      this.notificationService.success(
        `Library name: ${response.planName}\nCopied workouts: ${response.workouts}`)
    })
  }

  private openWarningDialog(plan, continueCallback): void {
    const dialogRef = this.dialog.open(TpCopyPlanWarningDialogComponent, {
      data: plan,
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        continueCallback.bind(this)()
      }
    });
  }

  private getMonday(date) {
    date = new Date(date);
    let day = date.getDay(),
      diff = date.getDate() - day + (day == 0 ? -6 : 1); // adjust when day is sunday
    return new Date(date.setDate(diff));
  }

  private getConfig() {
    this.configurationClient.getConfig().subscribe(config => {
      this.config = config.config
    })
  }

  private getStepModifiers() {
    this.configurationClient.getIntervalsStepModifiers().subscribe(values => {
      this.stepModifiers = values
    })
  }

  private getPlans() {
    this.planClient.getLibraries(Platform.TRAINING_PEAKS.key).pipe(
      map(plans => plans.map(plan => {
          return {name: plan.name, value: plan}
        })
      ),
      finalize(() => {
        this.loadingInProgress = false
        this.formGroup.enable()
      })
    ).subscribe(plans => this.plans = plans)
  }

  private onPlanChange() {
    this.formGroup.controls['plan'].valueChanges.pipe(
      filter(value => value!!)
    ).subscribe(value => {
      this.formGroup.patchValue({
        newName: value.name
      })
    })
  }
}
