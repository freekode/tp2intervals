import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators} from "@angular/forms";
import {formatDate} from "utils/date-formatter";
import {WorkoutClient} from "infrastructure/client/workout.client";
import {NotificationService} from "infrastructure/notification.service";
import {finalize} from "rxjs";
import {MatGridListModule} from "@angular/material/grid-list";
import {MatButtonModule} from "@angular/material/button";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatInputModule} from "@angular/material/input";
import {MatProgressBarModule} from "@angular/material/progress-bar";
import {NgIf} from "@angular/common";
import {MatDatepickerModule} from "@angular/material/datepicker";
import {MatNativeDateModule} from "@angular/material/core";
import {MatSnackBarModule} from "@angular/material/snack-bar";
import {MatSelectModule} from "@angular/material/select";
import {MatCheckboxModule} from "@angular/material/checkbox";
import {Platform} from "infrastructure/platform";
import {TrainerRoadTrainingTypes} from "app/trainer-road/trainer-road-training-types";

@Component({
  selector: 'tr-copy-calendar-to-library',
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
  ],
  templateUrl: './tr-copy-calendar-to-library.component.html',
  styleUrl: './tr-copy-calendar-to-library.component.scss'
})
export class TrCopyCalendarToLibraryComponent implements OnInit {
  private readonly selectedTrainingTypes = ['BIKE', 'VIRTUAL_BIKE', 'MTB', 'RUN'];
  private readonly direction = Platform.DIRECTION_TR_INT

  formGroup: FormGroup = this.formBuilder.group({
    name: ['My New Library', Validators.required],
    trainingTypes: [this.selectedTrainingTypes, Validators.required],
    startDate: [null, Validators.required],
    endDate: [null, Validators.required],
    isPlan: [true, Validators.required],
  });

  inProgress = false

  trainingTypes = TrainerRoadTrainingTypes.trainingTypes;

  readonly planType = [
    {name: 'Plan', value: true},
    {name: 'Folder', value: false}
  ]

  constructor(
    private formBuilder: FormBuilder,
    private workoutClient: WorkoutClient,
    private notificationService: NotificationService
  ) {
  }

  ngOnInit(): void {
  }

  copyWorkoutsSubmit() {
    this.inProgress = true
    let name = this.formGroup.value.name
    let trainingTypes = this.formGroup.value.trainingTypes
    let startDate = formatDate(this.formGroup.value.startDate)
    let endDate = formatDate(this.formGroup.value.endDate)
    let isPlan = this.formGroup.value.isPlan
    this.workoutClient.copyCalendarToLibrary(name, startDate, endDate, trainingTypes, this.direction, isPlan).pipe(
      finalize(() => this.inProgress = false)
    ).subscribe((response) => {
      this.notificationService.success(
        `Copied: ${response.copied}\n Filtered out: ${response.filteredOut}\n From ${response.startDate} to ${response.endDate}`)
    })
  }
}
