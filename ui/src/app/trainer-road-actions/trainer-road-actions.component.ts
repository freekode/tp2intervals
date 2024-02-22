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
import { MatNativeDateModule, MatOptionModule } from "@angular/material/core";
import { MatSnackBarModule } from "@angular/material/snack-bar";
import { Router } from "@angular/router";
import { NotificationService } from "infrastructure/notification.service";
import { finalize } from "rxjs";
import { ActivityClient } from "infrastructure/activity.client";
import { MatSelectModule } from "@angular/material/select";
import { formatDate } from "utils/date-formatter";

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
    MatSnackBarModule,
    MatOptionModule,
    MatSelectModule
  ],
  templateUrl: './trainer-road-actions.component.html',
  styleUrl: './trainer-road-actions.component.scss'
})
export class TrainerRoadActionsComponent implements OnInit {
  syncActivitiesFormGroup: FormGroup = this.formBuilder.group({
    trainingTypes: [null, Validators.required],
    startDate: [null, Validators.required],
    endDate: [null, Validators.required],
  });

  syncActivitiesInProgress = false

  trainingTypes = [
    {title: "Ride", value: "BIKE"},
    {title: "Virtual Ride", value: "VIRTUAL_BIKE"}
  ];

  private readonly selectedTrainingTypes = ['VIRTUAL_BIKE'];

  constructor(
    private router: Router,
    private formBuilder: FormBuilder,
    private activityClient: ActivityClient,
    private notificationService: NotificationService
  ) {
  }

  ngOnInit(): void {
    this.initFormValues()
  }

  syncActivitiesSubmit() {
    this.syncActivities(
      this.syncActivitiesFormGroup.value.startDate,
      this.syncActivitiesFormGroup.value.endDate
    )
  }

  syncTodayActivities() {
    this.syncActivities(new Date(), new Date())
  }

  private syncActivities(startDate, endDate) {
    this.syncActivitiesInProgress = true
    let startDateStr = formatDate(startDate)
    let endDateStr = formatDate(endDate)
    let trainingTypes = this.syncActivitiesFormGroup.value.trainingTypes
    this.activityClient.syncActivities(startDateStr, endDateStr, trainingTypes).pipe(
      finalize(() => this.syncActivitiesInProgress = false)
    ).subscribe((response) => {
      this.notificationService.success(
        `Synced ${response.synced} activity(ies)\n Filtered out ${response.filteredOut} workout(s)\n From ${response.startDate} to ${response.endDate}`)
    })
  }

  private initFormValues() {
    this.syncActivitiesFormGroup.patchValue({
      trainingTypes: this.selectedTrainingTypes,
    })
  }

}
