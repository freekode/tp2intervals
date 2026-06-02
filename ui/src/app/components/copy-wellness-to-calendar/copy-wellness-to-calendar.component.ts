import {Component, Input, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators} from "@angular/forms";
import {MatButtonModule} from "@angular/material/button";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatInputModule} from "@angular/material/input";
import {MatProgressBarModule} from "@angular/material/progress-bar";
import {MatDatepickerModule} from "@angular/material/datepicker";
import {MatNativeDateModule} from "@angular/material/core";
import {MatSnackBarModule} from "@angular/material/snack-bar";
import {MatSelectModule} from "@angular/material/select";
import {MatCheckboxModule} from "@angular/material/checkbox";
import {Platform} from "infrastructure/platform";
import {formatDate} from "utils/date-formatter";
import {MatDividerModule} from "@angular/material/divider";
import {MatListModule} from "@angular/material/list";
import {NgIf} from "@angular/common";
import {ConfigurationClient} from "infrastructure/client/configuration.client";
import {finalize, switchMap, tap} from "rxjs";
import {NotificationService} from "infrastructure/notification.service";
import {MatTooltipModule} from "@angular/material/tooltip";
import {TrainingTypes} from "infrastructure/training-types";
import {WellnessClient} from "infrastructure/client/wellness.client";

@Component({
  selector: 'copy-wellness-to-calendar',
  standalone: true,
  imports: [
    FormsModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    ReactiveFormsModule,
    MatProgressBarModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatSnackBarModule,
    MatSelectModule,
    MatCheckboxModule,
    MatDividerModule,
    MatListModule,
    NgIf,
    MatTooltipModule,
  ],
  templateUrl: './copy-wellness-to-calendar.component.html',
  styleUrl: './copy-wellness-to-calendar.component.scss'
})
export class CopyWellnessToCalendarComponent implements OnInit {
  readonly Platform = Platform;
  readonly todayDate = new Date()
  readonly tomorrowDate = new Date(new Date().getTime() + 24 * 60 * 60 * 1000)

  @Input() trainingTypes: any[] = []
  @Input() selectedTrainingTypes = ['WEIGHT']
  @Input() directions: any[] = []
  @Input() inProgress = false

  formGroup: FormGroup
  platformsInfo: any
  scheduleRequests: any[] = []

  constructor(
    private formBuilder: FormBuilder,
    private configurationClient: ConfigurationClient,
    private wellnessClient: WellnessClient,
    private notificationService: NotificationService
  ) {
  }

  ngOnInit(): void {
    this.configurationClient.getAllPlatformInfo().subscribe(value => {
      this.platformsInfo = value
    })
    this.formGroup = this.getFormGroup();
    this.loadScheduleRequests().subscribe()
  }

  submit() {
    let startDate = formatDate(this.formGroup.controls['startDate'].value)
    let endDate = formatDate(this.formGroup.controls['endDate'].value)
    this.copyWellness(startDate, endDate);
  }

  today() {
    this.copyWellnessForOneDay(formatDate(this.todayDate));
  }

  tomorrow() {
    this.copyWellnessForOneDay(formatDate(this.tomorrowDate));
  }

  scheduleToday() {
    let startDate = null
    let endDate = null
    let direction = this.formGroup.value.direction
    let trainingTypes = this.formGroup.value.trainingTypes
    let skipSynced = this.formGroup.value.skipSynced

    this.inProgress = true
    this.wellnessClient.scheduleCopyCalendarToCalendar(startDate, endDate, trainingTypes, skipSynced, direction).pipe(
      switchMap(() => this.loadScheduleRequests()),
      finalize(() => this.inProgress = false)
    ).subscribe(() => {
      this.notificationService.success(`Scheduled sync job`)
    })
  }

  mapTrainingTypesToTitles(values) {
    return values.map(value => TrainingTypes.getTitle(value))
  }

  private copyWellnessForOneDay(date) {
    this.copyWellness(date, date)
  }

  private copyWellness(startDate, endDate) {
    let direction = this.formGroup.value.direction
    let trainingTypes = this.formGroup.value.trainingTypes
    let skipSynced = this.formGroup.value.skipSynced

    this.inProgress = true
    this.wellnessClient.copyCalendarToCalendar(startDate, endDate, trainingTypes, skipSynced, direction).pipe(
      finalize(() => this.inProgress = false)
    ).subscribe((response) => {
      this.notificationService.success(
        `Wellness (Metrics): ${response.copied}\n From ${response.startDate} to ${response.endDate}`)
    })
  }

  private getFormGroup() {
    return this.formBuilder.group({
      direction: [this.directions[0].value, Validators.required],
      trainingTypes: [this.selectedTrainingTypes, Validators.required],
      startDate: [this.todayDate, Validators.required],
      endDate: [this.tomorrowDate, Validators.required],
      skipSynced: [true, Validators.required],
    })
  }

  private loadScheduleRequests() {
    return this.wellnessClient.getScheduleRequests().pipe(
      tap(values => {
          this.scheduleRequests = values.map(value => {
            return {id: value.id, request: JSON.parse(value.requestJson)}
          })
          console.log(this.scheduleRequests)
        }
      )
    )
  }

  deleteJob(jobId: any) {
    this.inProgress = true
    this.wellnessClient.deleteScheduleRequest(jobId).pipe(
      switchMap(() => this.loadScheduleRequests()),
      finalize(() => this.inProgress = false)
    ).subscribe(() => {
      this.notificationService.success(`Deleted job`)
    })
  }
}
