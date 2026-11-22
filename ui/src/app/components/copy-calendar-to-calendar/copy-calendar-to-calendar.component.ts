import {Component, Input, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators} from "@angular/forms";
import {MatButtonModule} from "@angular/material/button";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatInputModule} from "@angular/material/input";
import {MatProgressBarModule} from "@angular/material/progress-bar";
import {MatDatepickerModule} from "@angular/material/datepicker";
import {IsoDateModule} from "utils/iso-date.module";
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
import {ScheduledWorkoutRequest, TrainingPeaksPreviewInventory, TrainingPeaksPreviewSelection, TrainingPeaksSyncPreview, WorkoutClient} from "infrastructure/client/workout.client";
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
    IsoDateModule,
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
  readonly tomorrowDate = new Date(this.todayDate.getFullYear(), this.todayDate.getMonth(), this.todayDate.getDate() + 1)

  @Input() trainingTypes: any[] = []
  @Input() selectedTrainingTypes = ['BIKE', 'VIRTUAL_BIKE']
  @Input() directions: any[] = []
  @Input() inProgress = false

  formGroup!: FormGroup
  platformsInfo: any
  scheduleRequests: ScheduledWorkoutRequest[] = []
  previewResult?: {selection: TrainingPeaksPreviewSelection; inventory: TrainingPeaksPreviewInventory}
  readonly selectedLegacyLinks = new Map<string, number>();
  acknowledgeInconclusive = false;
  acknowledgedHold?: string;
  classification = '';
  inventorySort = 'date-desc';
  approvalMessage = '';
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
    this.approvalMessage = '';
    this.acknowledgeInconclusive = false;
    this.acknowledgedHold = undefined;
    this.classification = '';
    this.selectedLegacyLinks.clear();
    if (!token) {
      this.previewError = 'Enter the preview access token.';
      return;
    }
    const {startDate, endDate, trainingTypes} = this.formGroup.value;
    const selection = {startDate: formatDate(startDate), endDate: formatDate(endDate), types: [...trainingTypes]};
    this.inProgress = true;
    this.workoutClient.previewTrainingPeaksSync(selection, token)
      .pipe(finalize(() => this.inProgress = false))
      .subscribe({
        next: inventory => this.previewResult = {selection, inventory},
        error: error => {
          const message = error.error?.detail ?? error.error?.message;
          if (error.status === 401) this.previewError = 'The preview token was rejected.';
          else if (message === 'PREVIEW_DISABLED') this.previewError = 'Preview access is disabled on the server.';
          else if (message === 'TRAINING_PEAKS_AUTH_FAILED') this.previewError = 'TrainingPeaks rejected its stored credentials.';
          else if (message === 'INTERVALS_AUTH_FAILED') this.previewError = 'Intervals.icu rejected its stored credentials.';
          else if (message === 'TRAINING_PEAKS_CONFIGURATION_REQUIRED' || message === 'INTERVALS_CONFIGURATION_REQUIRED') {
            this.previewError = 'The provider configuration is missing or invalid.';
          } else if (error.status === 500) {
            this.previewError = 'Preview failed inside the application. Check the server log for the diagnostic code.';
          } else if (message === 'BASIC_TODAY_OR_TOMORROW_ONLY') {
            this.previewError = 'Select today or tomorrow for a Basic TrainingPeaks account.';
          } else if (message === 'ATHLETE_ACCOUNT_REQUIRED') {
            this.previewError = 'Preview requires a TrainingPeaks athlete account.';
          } else if (message === 'INVALID_DATE_RANGE') {
            this.previewError = 'Select a valid date range.';
          } else this.previewError = 'Preview could not read the selected calendars.';
        }
      });
  }

  get classificationCounts() {
    const counts = new Map<string, number>();
    for (const row of this.previewResult?.inventory.workouts ?? []) {
      counts.set(row.status, (counts.get(row.status) ?? 0) + 1);
    }
    return Object.entries(this.previewLabels).map(([status, label]) => ({status, label, count: counts.get(status) ?? 0}));
  }

  get visibleInventoryGroups() {
    const rows = (this.previewResult?.inventory.workouts ?? [])
      .filter(row => !this.classification || row.status === this.classification)
      .sort((a, b) => {
        if (this.inventorySort === 'status') {
          return a.status.localeCompare(b.status) || this.inventoryDate(b).localeCompare(this.inventoryDate(a));
        }
        return this.inventorySort === 'date-asc'
          ? this.inventoryDate(a).localeCompare(this.inventoryDate(b))
          : this.inventoryDate(b).localeCompare(this.inventoryDate(a));
      });
    return [
      {title: 'Current TrainingPeaks source workouts', rows: rows.filter(row => !!row.sourceDate)},
      {title: 'Historical destination and ledger entries', rows: rows.filter(row => !row.sourceDate)},
    ];
  }

  private inventoryDate(row: TrainingPeaksSyncPreview): string {
    return row.sourceDate ?? row.candidates.reduce((latest, event) => event.date && event.date > latest ? event.date : latest, '');
  }

  selectVisibleLegacyLinks(selected: boolean) {
    for (const group of this.visibleInventoryGroups) {
      for (const row of group.rows) {
        if (row.status === 'SINGLE_LEGACY_CANDIDATE' && row.sourceWorkoutId && row.candidates.length === 1) {
          this.selectLegacyLink(row.sourceWorkoutId, row.candidates[0].eventId, selected);
        }
      }
    }
  }

  resolveHold(row: TrainingPeaksSyncPreview, tokenInput: HTMLInputElement) {
    const result = this.previewResult;
    const id = row.sourceWorkoutId;
    if (!result || !id || !row.canResolveUnmatchedHold || this.acknowledgedHold !== id ||
        result.inventory.previewOnly || result.inventory.activationEnabled || this.inProgress) return;
    const token = tokenInput.value.trim();
    tokenInput.value = '';
    if (!token) {
      this.previewError = 'Enter the preview access token again to resolve this hold.';
      return;
    }
    this.previewError = '';
    this.approvalMessage = '';
    this.inProgress = true;
    this.workoutClient.resolveTrainingPeaksHold(result.selection, result.inventory.revision, id, token)
      .pipe(finalize(() => this.inProgress = false)).subscribe({
        next: reply => {
          this.previewResult = undefined;
          this.approvalMessage = `Created Intervals.icu event ${reply.eventId}. Preview again to verify the link.`;
        },
        error: error => {
          const code = error.error?.detail ?? error.error?.message;
          this.previewResult = undefined;
          this.previewError = code === 'HOLD_WRITE_UNCERTAIN'
            ? 'The write outcome is uncertain. Check the destination and preview again. Do not retry the create.'
            : error.status === 401
              ? 'The preview token was rejected.'
              : 'Resolution was blocked. Check the account, source date and server settings, then run a fresh preview.';
        },
      });
    this.acknowledgedHold = undefined;
  }

  selectLegacyLink(sourceId: string, eventId: number, selected: boolean) {
    if (selected) this.selectedLegacyLinks.set(sourceId, eventId);
    else this.selectedLegacyLinks.delete(sourceId);
  }

  approveBaseline(tokenInput: HTMLInputElement) {
    const result = this.previewResult;
    const token = tokenInput.value.trim();
    tokenInput.value = '';
    if (!result || !this.acknowledgeInconclusive) return;
    if (!token) {
      this.previewError = 'Enter the preview access token again to approve.';
      return;
    }
    this.previewError = '';
    this.inProgress = true;
    this.workoutClient.approveTrainingPeaksBaseline(
      result.selection, result.inventory.revision, this.acknowledgeInconclusive,
      Object.fromEntries(this.selectedLegacyLinks), token
    ).pipe(finalize(() => this.inProgress = false)).subscribe({
      next: reply => {
        this.approvalMessage = `Baseline approved at ${reply.approvedAt} UTC. ${reply.heldWorkouts} reviewed workouts held.`;
        this.previewResult = undefined;
      },
      error: error => {
        if (error.status === 409) {
          this.previewResult = undefined;
          this.previewError = 'The inventory changed. Preview it again before approving.';
        } else if (error.status === 401) {
          this.previewError = 'The preview token was rejected.';
        } else {
          this.previewError = 'Baseline approval failed. Check preview-only and activation settings.';
        }
      }
    });
  }

  get effectiveSkipSynced(): boolean {
    return this.isTrainingPeaksForward || this.formGroup.value.skipSynced;
  }

  scheduleToday() {
    let startDate = null
    let endDate = null
    let direction = this.formGroup.value.direction
    let trainingTypes = this.formGroup.value.trainingTypes
    const skipSynced = this.effectiveSkipSynced

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
    const skipSynced = this.effectiveSkipSynced

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
