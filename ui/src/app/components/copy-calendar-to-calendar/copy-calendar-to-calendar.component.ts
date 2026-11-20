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
import {ScheduledWorkoutRequest, TrainingPeaksSyncPreview, WorkoutClient} from "infrastructure/client/workout.client";
import {NotificationService} from "infrastructure/notification.service";
import {MatTooltipModule} from "@angular/material/tooltip";
import {TrainingTypes} from "infrastructure/training-types";

@Component({
  selector: 'copy-calendar-to-calendar',
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
  templateUrl: './copy-calendar-to-calendar.component.html',
  styleUrl: './copy-calendar-to-calendar.component.scss'
})
export class CopyCalendarToCalendarComponent implements OnInit {
  readonly Platform = Platform;
  readonly todayDate = new Date()
  readonly tomorrowDate = new Date(new Date().getTime() + 24 * 60 * 60 * 1000)

  @Input() trainingTypes: any[] = []
  @Input() selectedTrainingTypes = ['BIKE', 'VIRTUAL_BIKE']
  @Input() directions: any[] = []
  @Input() inProgress = false

  formGroup: FormGroup
  platformsInfo: any
  scheduleRequests: ScheduledWorkoutRequest[] = []
  previewResult?: {start: string; end: string; workouts: TrainingPeaksSyncPreview[]}
  previewError = ''
  syncReview: {sourceWorkoutId: string; reason: string; remoteEventId?: number}[] = []

  readonly previewLabels: Record<string, string> = {
    VERIFIED_LINK: 'Verified link',
    SINGLE_LEGACY_CANDIDATE: 'Single legacy candidate',
    MULTIPLE_LEGACY_CANDIDATES: 'Multiple legacy candidates',
    NO_MATCH_OBSERVED: 'No match observed (inconclusive)',
    UNRESOLVED_CLAIM: 'Unresolved claim',
    BROKEN_OR_UNVERIFIED_LINK: 'Broken or unverified link',
  }
  readonly scheduleWarnings: Record<string, string> = {
    PREVIEW_ONLY: 'Paused: the server is in preview-only mode.',
    SKIP_SYNCED_FALSE: 'Blocked: Skip already synced workouts is off.',
    SKIP_SYNCED_MISSING: 'Blocked: the Skip synced setting is missing.',
    SKIP_SYNCED_MALFORMED: 'Blocked: the Skip synced setting is invalid.',
    MALFORMED_REQUEST: 'Blocked: the saved schedule is invalid.',
    FORWARD_SYNC_PAUSED: 'Paused: enable scheduled forward sync after reviewing the inventory.',
  }

  get isTrainingPeaksForward(): boolean {
    const direction = this.formGroup.value.direction;
    return direction?.sourcePlatform === Platform.TRAINING_PEAKS.key &&
      direction?.targetPlatform === Platform.INTERVALS.key;
  }

  constructor(
    private formBuilder: FormBuilder,
    private configurationClient: ConfigurationClient,
    private workoutClient: WorkoutClient,
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
    this.copyWorkouts(startDate, endDate);
  }

  today() {
    this.copyWorkoutsForOneDay(formatDate(this.todayDate));
  }

  tomorrow() {
    this.copyWorkoutsForOneDay(formatDate(this.tomorrowDate));
  }

  preview(tokenInput: HTMLInputElement) {
    const token = tokenInput.value.trim();
    tokenInput.value = '';
    this.previewResult = undefined;
    this.previewError = '';
    if (!token) {
      this.previewError = 'Enter the preview access token.';
      return;
    }
    const {startDate, endDate, trainingTypes} = this.formGroup.value;
    const start = formatDate(startDate);
    const end = formatDate(endDate);
    this.inProgress = true;
    this.workoutClient.previewTrainingPeaksSync(start, end, trainingTypes, token)
      .pipe(finalize(() => this.inProgress = false))
      .subscribe({
        next: workouts => this.previewResult = {start, end, workouts},
        error: error => {
          const message = error.status === 400 ? error.error?.message : undefined;
          if (error.status === 401) this.previewError = 'The preview token was rejected.';
          else if (error.status === 503) this.previewError = 'Preview access is disabled on the server.';
          else if (message === 'BASIC_TODAY_OR_TOMORROW_ONLY') {
            this.previewError = 'Select today or tomorrow for a Basic TrainingPeaks account.';
          } else if (message === 'ATHLETE_ACCOUNT_REQUIRED') {
            this.previewError = 'Preview requires a TrainingPeaks athlete account.';
          } else if (message === 'INVALID_DATE_RANGE') {
            this.previewError = 'Select a valid date range.';
          } else this.previewError = 'Preview could not read the selected calendars.';
        }
      });
  }

  scheduleToday() {
    let startDate = null
    let endDate = null
    let direction = this.formGroup.value.direction
    let trainingTypes = this.formGroup.value.trainingTypes
    let skipSynced = this.formGroup.value.skipSynced

    this.inProgress = true
    this.workoutClient.scheduleCopyCalendarToCalendar(startDate, endDate, trainingTypes, skipSynced, direction).pipe(
      switchMap(() => this.loadScheduleRequests()),
      finalize(() => this.inProgress = false)
    ).subscribe(() => {
      this.notificationService.success(`Scheduled sync job`)
    })
  }

  mapTrainingTypesToTitles(values) {
    return values.map(value => TrainingTypes.getTitle(value))
  }

  private copyWorkoutsForOneDay(date) {
    this.copyWorkouts(date, date)
  }

  private copyWorkouts(startDate, endDate) {
    let direction = this.formGroup.value.direction
    let trainingTypes = this.formGroup.value.trainingTypes
    let skipSynced = this.formGroup.value.skipSynced

    this.inProgress = true
    this.syncReview = [];
    this.workoutClient.copyCalendarToCalendar(startDate, endDate, trainingTypes, skipSynced, direction).pipe(
      finalize(() => this.inProgress = false)
    ).subscribe((response) => {
      this.syncReview = response.syncReview ?? [];
      this.notificationService.success(
        `Planned: ${response.copied}\n Filtered out: ${response.filteredOut}\n From ${response.startDate} to ${response.endDate}`)
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
    return this.workoutClient.getScheduleRequests().pipe(tap(values => this.scheduleRequests = values));
  }

  deleteJob(jobId: any) {
    this.inProgress = true
    this.workoutClient.deleteScheduleRequest(jobId).pipe(
      switchMap(() => this.loadScheduleRequests()),
      finalize(() => this.inProgress = false)
    ).subscribe(() => {
      this.notificationService.success(`Deleted job`)
    })
  }
}
