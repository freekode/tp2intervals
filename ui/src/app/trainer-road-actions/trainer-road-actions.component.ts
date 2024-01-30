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
import { Router } from "@angular/router";
import { NotificationService } from "infrastructure/notification.service";
import { finalize } from "rxjs";
import { ActivityClient } from "infrastructure/activity.client";

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
export class TrainerRoadActionsComponent implements OnInit {
  syncActivitiesFormGroup: FormGroup = this.formBuilder.group({
    startDate: [null, Validators.required],
    endDate: [null, Validators.required],
  });

  jobStarted = true
  syncActivitiesInProgress = false

  constructor(
    private router: Router,
    private formBuilder: FormBuilder,
    private activityClient: ActivityClient,
    private notificationService: NotificationService
  ) {
  }

  ngOnInit(): void {
    // this.activityClient.getJobSyncActivities().subscribe(response => {
    //   this.jobStarted = !!response?.id
    // })
  }

  syncActivitiesSubmit() {
    this.syncActivitiesInProgress = true
    let startDate = this.syncActivitiesFormGroup.value.startDate.toISOString().split('T')[0]
    let endDate = this.syncActivitiesFormGroup.value.endDate.toISOString().split('T')[0]
    this.activityClient.syncActivities(startDate, endDate).pipe(
      finalize(() => this.syncActivitiesInProgress = false)
    ).subscribe(() => {
      this.notificationService.success('Activities are synced')
    })
  }

  startJob() {
    this.activityClient.startJobSyncActivities().subscribe(() => {
      this.jobStarted = true
    })
  }

  stopJob() {
    this.activityClient.stopJobSyncActivities().subscribe(() => {
      this.jobStarted = false
    })
  }
}
