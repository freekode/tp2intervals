import { Component, OnInit } from '@angular/core';
import { MatGridListModule } from "@angular/material/grid-list";
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from "@angular/forms";
import { MatButtonModule } from "@angular/material/button";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatInputModule } from "@angular/material/input";
import { MatProgressBarModule } from "@angular/material/progress-bar";
import { NgIf } from "@angular/common";
import { MatDatepickerModule } from "@angular/material/datepicker";
import { MatNativeDateModule } from "@angular/material/core";
import { MatSnackBarModule } from "@angular/material/snack-bar";
import { MatSelectModule } from "@angular/material/select";
import { MatCheckboxModule } from "@angular/material/checkbox";
import {
  TpCopyLibraryContainerComponent
} from "app/training-peaks/tp-copy-library-container/tp-copy-library-container.component";
import { formatDate } from "utils/date-formatter";
import { WorkoutClient } from "infrastructure/client/workout.client";
import { ConfigurationClient } from "infrastructure/client/configuration.client";
import { NotificationService } from "infrastructure/notification.service";
import { finalize } from "rxjs";
import { Platform } from "infrastructure/platform";

@Component({
  selector: 'tp-copy-calendar-to-calendar',
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
    TpCopyLibraryContainerComponent
  ],
  templateUrl: './tp-copy-calendar-to-calendar.component.html',
  styleUrl: './tp-copy-calendar-to-calendar.component.scss'
})
export class TpCopyCalendarToCalendarComponent implements OnInit {
  private readonly todayDate = formatDate(new Date())
  private readonly tomorrowDate = formatDate(new Date(new Date().setDate(new Date().getDate() + 1)))
  private readonly selectedTrainingTypes = ['BIKE', 'VIRTUAL_BIKE', 'MTB', 'RUN'];
  private readonly direction = Platform.DIRECTION_INT_TP

  formGroup: FormGroup = this.formBuilder.group({
    trainingTypes: [this.selectedTrainingTypes, Validators.required],
    startDate: [this.todayDate, Validators.required],
    endDate: [this.tomorrowDate, Validators.required],
    skipSynced: [true, Validators.required],
  });

  trainingTypes: any[];

  inProgress = false

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
    })
  }

  submit() {
    this.inProgress = true
    let startDate = this.todayDate
    let endDate = this.tomorrowDate
    let trainingTypes = this.formGroup.value.trainingTypes
    let skipSynced = this.formGroup.value.skipSynced
    this.workoutClient.copyCalendarToCalendar(startDate, endDate, trainingTypes, skipSynced, this.direction).pipe(
      finalize(() => this.inProgress = false)
    ).subscribe((response) => {
      this.notificationService.success(
        `Planned: ${response.copied}\n Filtered out: ${response.filteredOut}\n From ${response.startDate} to ${response.endDate}`)
    })
  }
}
