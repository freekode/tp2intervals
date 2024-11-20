import {Component, OnInit} from '@angular/core';
import {MatButtonModule} from "@angular/material/button";
import {MatDatepickerModule} from "@angular/material/datepicker";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatInputModule} from "@angular/material/input";
import {MatOptionModule} from "@angular/material/core";
import {MatProgressBarModule} from "@angular/material/progress-bar";
import {MatSelectModule} from "@angular/material/select";
import {NgIf} from "@angular/common";
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from "@angular/forms";
import {Platform} from "infrastructure/platform";
import {ConfigurationClient} from "infrastructure/client/configuration.client";
import {NotificationService} from "infrastructure/notification.service";
import {formatDate} from "utils/date-formatter";
import {finalize} from "rxjs";
import {ActivityClient} from "infrastructure/client/activity.client";

@Component({
  selector: 'app-strava-copy-activities',
  standalone: true,
  imports: [
    MatButtonModule,
    MatDatepickerModule,
    MatFormFieldModule,
    MatInputModule,
    MatOptionModule,
    MatProgressBarModule,
    MatSelectModule,
    NgIf,
    ReactiveFormsModule
  ],
  templateUrl: './strava-copy-activities.component.html',
  styleUrl: './strava-copy-activities.component.scss'
})
export class StravaCopyActivitiesComponent implements OnInit {
  private readonly selectedTrainingTypes = ['BIKE', 'VIRTUAL_BIKE', 'MTB', 'RUN'];
  private readonly direction = {
    sourcePlatform: Platform.STRAVA.key, targetPlatform: Platform.INTERVALS.key
  }

  formGroup: FormGroup = this.formBuilder.group({
    trainingTypes: [this.selectedTrainingTypes, Validators.required],
    startDate: [null, Validators.required],
    endDate: [null, Validators.required],
  });

  inProgress = false

  trainingTypes: any[];

  constructor(
    private formBuilder: FormBuilder,
    private activityClient: ActivityClient,
    private configurationClient: ConfigurationClient,
    private notificationService: NotificationService
  ) {
  }

  ngOnInit(): void {
    this.configurationClient.getTrainingTypes().subscribe(types => {
      this.trainingTypes = types
    })
  }

  copyWorkoutsSubmit() {
    this.inProgress = true
    let trainingTypes = this.formGroup.value.trainingTypes
    let startDate = formatDate(this.formGroup.value.startDate)
    let endDate = formatDate(this.formGroup.value.endDate)
    this.activityClient.copyActivities(startDate, endDate, trainingTypes, this.direction).pipe(
      finalize(() => this.inProgress = false)
    ).subscribe((response) => {
      this.notificationService.success(
        `Copied: ${response.copied}\n Filtered out: ${response.filteredOut}\n From ${response.startDate} to ${response.endDate}`)
    })
  }
}
