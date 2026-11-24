import {Component, Input, OnDestroy, OnInit} from '@angular/core';
import {HttpErrorResponse} from '@angular/common/http';
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
import {MatRadioModule} from "@angular/material/radio";
import {MatDialog} from "@angular/material/dialog";
import {Platform} from "infrastructure/platform";
import {formatDate} from "utils/date-formatter";
import {MatDividerModule} from "@angular/material/divider";
import {DatePipe, NgIf} from "@angular/common";
import {ConfigurationClient} from "infrastructure/client/configuration.client";
import {catchError, EMPTY, filter, finalize, Subscription, switchMap, tap, throwError, timer} from "rxjs";
import {WorkoutClient} from "infrastructure/client/workout.client";
import {NotificationService} from "infrastructure/notification.service";
import {MatTooltipModule} from "@angular/material/tooltip";
import {TrainingTypes} from "infrastructure/training-types";
import {describeSkippedWorkout, SyncPreviewDialogComponent} from "./sync-preview-dialog.component";

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
    MatRadioModule,
    MatDividerModule,
    NgIf,
    DatePipe,
    MatTooltipModule,
  ],
  templateUrl: './copy-calendar-to-calendar.component.html',
  styleUrl: './copy-calendar-to-calendar.component.scss'
})
export class CopyCalendarToCalendarComponent implements OnInit, OnDestroy {
  readonly Platform = Platform;
  readonly describeSkippedWorkout = describeSkippedWorkout;
  readonly todayDate = new Date()
  readonly tomorrowDate = new Date(new Date().getTime() + 24 * 60 * 60 * 1000)

  @Input() trainingTypes: any[] = []
  @Input() selectedTrainingTypes = ['BIKE', 'VIRTUAL_BIKE']
  @Input() directions: any[] = []
  @Input() inProgress = false

  formGroup: FormGroup
  platformsInfo: any
  scheduleRequests: any[] = []
  private schedulePoll?: Subscription

  constructor(
    private formBuilder: FormBuilder,
    private configurationClient: ConfigurationClient,
    private workoutClient: WorkoutClient,
    private notificationService: NotificationService,
    private dialog: MatDialog,
  ) {
  }

  ngOnInit(): void {
    this.configurationClient.getAllPlatformInfo().subscribe(value => {
      this.platformsInfo = value
    })
    this.formGroup = this.getFormGroup();
    if (this.isReconciliationSupported(this.formGroup.value.direction)) {
      this.applyHorizon();
    }
    this.loadScheduleRequests().subscribe()
    this.schedulePoll = timer(30000, 30000).pipe(
      switchMap(() => this.loadScheduleRequests().pipe(
        catchError(error => error instanceof HttpErrorResponse ? EMPTY : throwError(() => error)),
      )),
    ).subscribe()
  }

  ngOnDestroy(): void {
    this.schedulePoll?.unsubscribe()
  }

  submit() {
    if (this.isReconciliationSupported(this.formGroup.value.direction)) this.applyHorizon()
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

  scheduleToday() {
    let startDate = null
    let endDate = null
    let direction = this.formGroup.value.direction
    let trainingTypes = this.formGroup.value.trainingTypes
    let skipSynced = this.formGroup.value.skipSynced
    let defaultStartTime = this.formGroup.value.defaultStartTime || null
    let trainerRoadSyncMode = this.isReconciliationSupported(direction) ? this.formGroup.value.trainerRoadSyncMode : null
    let allowStaleRemoval = trainerRoadSyncMode === 'MIRROR' && this.formGroup.value.allowStaleRemoval

    this.inProgress = true
    this.workoutClient.scheduleCopyCalendarToCalendar(startDate, endDate, trainingTypes, skipSynced, defaultStartTime, trainerRoadSyncMode, allowStaleRemoval, direction).pipe(
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
    let defaultStartTime = this.formGroup.value.defaultStartTime || null
    let trainerRoadSyncMode = this.isReconciliationSupported(direction) ? this.formGroup.value.trainerRoadSyncMode : null
    let allowStaleRemoval = trainerRoadSyncMode === 'MIRROR' && this.formGroup.value.allowStaleRemoval

    this.inProgress = true
    const execute = (expectedActions = null, expectedUnidentifiedActivityDates = null) => this.workoutClient.copyCalendarToCalendar(startDate, endDate, trainingTypes, skipSynced, defaultStartTime, trainerRoadSyncMode, allowStaleRemoval, direction, expectedActions, expectedUnidentifiedActivityDates)
    const sync = trainerRoadSyncMode
      ? this.workoutClient.previewCalendarToCalendar(startDate, endDate, trainingTypes, skipSynced, defaultStartTime, trainerRoadSyncMode, allowStaleRemoval, direction).pipe(
          switchMap(preview => this.dialog.open(SyncPreviewDialogComponent, {data: {...preview, trainerRoadSyncMode}}).afterClosed().pipe(
            filter(confirmed => confirmed === true),
            switchMap(() => execute(preview.previewActions, preview.unidentifiedActivityDates ?? [])),
          )),
        )
      : execute()
    sync.pipe(
      finalize(() => this.inProgress = false)
    ).subscribe((response) => {
      this.notificationService.success(
        `Copied: ${response.copied}<br>${trainerRoadSyncMode === 'ADD_MISSING' ? 'Existing matches (not checked for changes)' : trainerRoadSyncMode === 'MIRROR' ? 'Unchanged matches' : 'Already synced'}: ${response.alreadySynced}${trainerRoadSyncMode ? `<br>Updated: ${response.updated}<br>Removed: ${response.removed}${response.skipped ? `<br>Skipped (ambiguous or protected): ${response.skipped}` : ''}` : ''}${response.unidentifiedActivityDates?.length ? `<br>Activities without workout IDs on ${response.unidentifiedActivityDates.join(', ')} require manual review.${trainerRoadSyncMode === 'MIRROR' ? ' Marked events may have been removed even if they correspond to these activities.' : ''}` : ''}<br>Filtered out by workout type: ${response.filteredOut}<br>From ${response.startDate} to ${response.endDate}`)
    })
  }

  isReconciliationSupported(direction: {sourcePlatform: string, targetPlatform: string}): boolean {
    return direction?.sourcePlatform === Platform.TRAINER_ROAD.key && direction?.targetPlatform === Platform.INTERVALS.key
  }

  isMirrorBlocked(): boolean {
    return this.isReconciliationSupported(this.formGroup.value.direction) &&
      this.formGroup.value.trainerRoadSyncMode === 'MIRROR' && !this.formGroup.value.allowStaleRemoval
  }

  applyHorizon(): void {
    const days = Number(this.formGroup.value.horizon)
    if (!days) return
    const start = new Date()
    const end = new Date(start)
    end.setDate(end.getDate() + days - 1)
    this.formGroup.patchValue({startDate: start, endDate: end})
  }

  directionChanged(): void {
    if (!this.isReconciliationSupported(this.formGroup.value.direction)) {
      this.formGroup.patchValue({allowStaleRemoval: false})
    }
    if (this.isReconciliationSupported(this.formGroup.value.direction)) this.applyHorizon()
  }

  syncModeChanged(): void {
    if (this.formGroup.value.trainerRoadSyncMode !== 'MIRROR') {
      this.formGroup.patchValue({allowStaleRemoval: false})
    }
  }

  private getFormGroup() {
    return this.formBuilder.group({
      direction: [this.directions[0].value, Validators.required],
      trainingTypes: [this.selectedTrainingTypes, Validators.required],
      startDate: [this.todayDate, Validators.required],
      endDate: [this.tomorrowDate, Validators.required],
      skipSynced: [true, Validators.required],
      trainerRoadSyncMode: ['ADD_MISSING', Validators.required],
      allowStaleRemoval: [false],
      horizon: ['2', Validators.required],
      defaultStartTime: [null, Validators.pattern('^([01][0-9]|2[0-3]):[0-5][0-9]$')],
    })
  }

  private loadScheduleRequests() {
    return this.workoutClient.getScheduleRequests().pipe(
      tap(values => {
          this.scheduleRequests = values.map(value => {
            return {id: value.id, request: JSON.parse(value.requestJson), lastResult: value.lastResultJson ? JSON.parse(value.lastResultJson) : null}
          })
        }
      )
    )
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
