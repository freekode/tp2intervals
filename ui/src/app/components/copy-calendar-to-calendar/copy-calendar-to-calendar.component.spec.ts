import {TestBed} from '@angular/core/testing';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {MatDialog} from '@angular/material/dialog';
import {MatTooltip} from '@angular/material/tooltip';
import {By} from '@angular/platform-browser';
import {of, throwError} from 'rxjs';
import {ConfigurationClient} from 'infrastructure/client/configuration.client';
import {WorkoutClient} from 'infrastructure/client/workout.client';
import {NotificationService} from 'infrastructure/notification.service';
import {Platform} from 'infrastructure/platform';
import {CopyCalendarToCalendarComponent} from './copy-calendar-to-calendar.component';

describe('Mirror confirmation', () => {
  it('uses the current local day for today and tomorrow after midnight', () => {
    jasmine.clock().install();
    try {
      jasmine.clock().mockDate(new Date(2026, 10, 24, 23, 55));
      const workoutClient = {
        getScheduleRequests: () => of([]),
        previewCalendarToCalendar: jasmine.createSpy().and.returnValue(of({})),
      };
      TestBed.configureTestingModule({
        imports: [CopyCalendarToCalendarComponent, NoopAnimationsModule],
        providers: [
          {provide: ConfigurationClient, useValue: {getAllPlatformInfo: () => of({})}},
          {provide: WorkoutClient, useValue: workoutClient},
          {provide: NotificationService, useValue: {}},
          {provide: MatDialog, useValue: {open: () => ({afterClosed: () => of(false)})}},
        ],
      });
      const fixture = TestBed.createComponent(CopyCalendarToCalendarComponent);
      fixture.componentInstance.directions = [{title: 'TrainerRoad to Intervals', value: Platform.DIRECTION_TR_INT}];
      fixture.detectChanges();
      jasmine.clock().mockDate(new Date(2026, 10, 25, 0, 5));

      fixture.componentInstance.today();
      fixture.componentInstance.tomorrow();

      expect(workoutClient.previewCalendarToCalendar.calls.argsFor(0).slice(0, 2)).toEqual(['2026-11-25', '2026-11-25']);
      expect(workoutClient.previewCalendarToCalendar.calls.argsFor(1).slice(0, 2)).toEqual(['2026-11-26', '2026-11-26']);
      fixture.destroy();
    } finally {
      jasmine.clock().uninstall();
    }
  });

  it('reports a created job even when refreshing the job list fails', () => {
    const notification = {success: jasmine.createSpy('success'), error: jasmine.createSpy('error')};
    const workoutClient = {
      getScheduleRequests: jasmine.createSpy().and.returnValues(of([]), throwError(() => new Error('unavailable'))),
      scheduleCopyCalendarToCalendar: jasmine.createSpy().and.returnValue(of(null)),
    };
    TestBed.configureTestingModule({
      imports: [CopyCalendarToCalendarComponent, NoopAnimationsModule],
      providers: [
        {provide: ConfigurationClient, useValue: {getAllPlatformInfo: () => of({})}},
        {provide: WorkoutClient, useValue: workoutClient},
        {provide: NotificationService, useValue: notification},
        {provide: MatDialog, useValue: {}},
      ],
    });
    const fixture = TestBed.createComponent(CopyCalendarToCalendarComponent);
    fixture.componentInstance.directions = [{title: 'TrainerRoad to Intervals', value: Platform.DIRECTION_TR_INT}];
    fixture.detectChanges();

    fixture.componentInstance.scheduleToday();

    expect(notification.success).toHaveBeenCalledOnceWith('Scheduled sync job');
    expect(notification.error).toHaveBeenCalledOnceWith(jasmine.stringMatching(/was scheduled.*will run until deleted/));
    expect(fixture.componentInstance.inProgress).toBeFalse();
    fixture.destroy();
  });

  it('requires confirmation inside the selected option and clears it when leaving Mirror', () => {
    TestBed.configureTestingModule({
      imports: [CopyCalendarToCalendarComponent, NoopAnimationsModule],
      providers: [
        {provide: ConfigurationClient, useValue: {getAllPlatformInfo: () => of({})}},
        {provide: WorkoutClient, useValue: {getScheduleRequests: () => of([])}},
        {provide: NotificationService, useValue: {}},
        {provide: MatDialog, useValue: {}},
      ],
    });
    const fixture = TestBed.createComponent(CopyCalendarToCalendarComponent);
    fixture.componentInstance.directions = [
      {title: 'TrainerRoad to Intervals', value: Platform.DIRECTION_TR_INT},
      {title: 'TrainingPeaks to Intervals', value: Platform.DIRECTION_TP_INT},
    ];
    fixture.componentInstance.trainingTypes = [{title: 'Ride', value: 'BIKE'}];
    fixture.detectChanges();

    const root: HTMLElement = fixture.nativeElement;
    const timeHelp = fixture.debugElement.query(By.css('button[aria-label="About the default start time"]'));
    expect(timeHelp.injector.get(MatTooltip).message).toContain('TrainerRoad\'s time takes priority');
    const selectMode = (mode: string) => {
      root.querySelector<HTMLInputElement>(`mat-radio-button input[value="${mode}"]`)!.click();
      fixture.detectChanges();
    };
    const preview = () => root.querySelector<HTMLButtonElement>('#btn-confirm')!;
    const confirm = () => root.querySelector<HTMLInputElement>('mat-checkbox input')!;

    expect(root.querySelector('mat-checkbox')).toBeNull();
    selectMode('MIRROR');
    const mirrorCard = root.querySelectorAll('.sync-mode-option')[1];
    expect(mirrorCard.querySelector('mat-checkbox')).not.toBeNull();
    expect(mirrorCard.querySelector('mat-radio-button mat-checkbox')).toBeNull();
    expect(root.querySelector('#mirror-risk-details')!.textContent)
      .toContain('including manual edits and workouts not created by this app');
    expect(confirm().checked).toBeFalse();
    expect(preview().disabled).toBeTrue();

    confirm().click();
    fixture.detectChanges();
    expect(preview().disabled).toBeFalse();
    selectMode('ADD_MISSING');
    selectMode('MIRROR');
    expect(confirm().checked).toBeFalse();
    expect(preview().disabled).toBeTrue();

    confirm().click();
    fixture.detectChanges();
    fixture.componentInstance.formGroup.patchValue({direction: Platform.DIRECTION_TP_INT});
    fixture.componentInstance.directionChanged();
    fixture.detectChanges();
    expect(root.querySelector('button[aria-label="About the default start time"]')).toBeNull();
    fixture.componentInstance.formGroup.patchValue({direction: Platform.DIRECTION_TR_INT});
    fixture.componentInstance.directionChanged();
    fixture.detectChanges();
    expect(confirm().checked).toBeFalse();
    expect(preview().disabled).toBeTrue();
    fixture.destroy();
  });

  it('shows each scheduled job default time or that none is configured', () => {
    const request = {
      ...Platform.DIRECTION_TR_INT,
      types: ['BIKE'],
      trainerRoadSyncMode: 'MIRROR',
      allowStaleRemoval: true,
      mirrorByTrainerRoadId: true,
      allowUnidentifiedSourceRemoval: true,
    };
    TestBed.configureTestingModule({
      imports: [CopyCalendarToCalendarComponent, NoopAnimationsModule],
      providers: [
        {provide: ConfigurationClient, useValue: {getAllPlatformInfo: () => of({})}},
        {provide: WorkoutClient, useValue: {getScheduleRequests: () => of([
          {id: 1, requestJson: JSON.stringify({...request, defaultStartTime: '12:01'})},
          {id: 2, requestJson: JSON.stringify({...request, defaultStartTime: '00:00'})},
          {id: 3, requestJson: JSON.stringify({...request, defaultStartTime: null})},
          {id: 4, requestJson: JSON.stringify({...request, defaultStartTime: ''})},
          {id: 5, requestJson: JSON.stringify(request)},
        ])}},
        {provide: NotificationService, useValue: {}},
        {provide: MatDialog, useValue: {}},
      ],
    });
    const fixture = TestBed.createComponent(CopyCalendarToCalendarComponent);
    fixture.componentInstance.directions = [{title: 'TrainerRoad to Intervals', value: Platform.DIRECTION_TR_INT}];
    fixture.componentInstance.trainingTypes = [{title: 'Ride', value: 'BIKE'}];
    fixture.detectChanges();

    const jobs: NodeListOf<HTMLElement> = fixture.nativeElement.querySelectorAll('.scheduled-job');
    expect(jobs.length).toBe(5);
    expect(jobs[0].textContent).toContain('Default time: 12:01');
    expect(jobs[1].textContent).toContain('Default time: 00:00');
    for (const job of Array.from(jobs).slice(2)) {
      expect(job.textContent).toContain('No default time configured');
    }
    fixture.destroy();
  });

  it('requires new Mirror consent when switching to TrainingPeaks', () => {
    TestBed.configureTestingModule({
      imports: [CopyCalendarToCalendarComponent, NoopAnimationsModule],
      providers: [
        {provide: ConfigurationClient, useValue: {getAllPlatformInfo: () => of({})}},
        {provide: WorkoutClient, useValue: {getScheduleRequests: () => of([])}},
        {provide: NotificationService, useValue: {}},
        {provide: MatDialog, useValue: {}},
      ],
    });
    const fixture = TestBed.createComponent(CopyCalendarToCalendarComponent);
    fixture.componentInstance.directions = [
      {title: 'TrainerRoad to Intervals', value: Platform.DIRECTION_TR_INT},
      {title: 'TrainerRoad to TrainingPeaks', value: Platform.DIRECTION_TR_TP},
    ];
    fixture.detectChanges();
    fixture.componentInstance.formGroup.patchValue({trainerRoadSyncMode: 'MIRROR', allowStaleRemoval: true});
    fixture.componentInstance.formGroup.patchValue({direction: Platform.DIRECTION_TR_TP});
    fixture.componentInstance.directionChanged();
    fixture.detectChanges();

    expect(fixture.componentInstance.isReconciliationSupported(Platform.DIRECTION_TR_TP)).toBeTrue();
    expect(fixture.componentInstance.isMirrorBlocked()).toBeTrue();
    expect(fixture.nativeElement.textContent).toContain('a concurrent completion may still race');
    expect(fixture.nativeElement.textContent).toContain('TrainingPeaks Premium');
    expect(fixture.nativeElement.textContent).toContain('keeps the existing TrainingPeaks planned time');
    expect(fixture.nativeElement.textContent).not.toContain('Not used when syncing to TrainingPeaks');
    fixture.destroy();
  });

  it('sends previewed unidentified dates when confirming Mirror', () => {
    const preview = {
      copied: 0, updated: 0, removed: 1, alreadySynced: 0, skipped: 0, filteredOut: 0,
      startDate: '2026-11-24', endDate: '2026-11-24',
      unidentifiedActivityDates: ['2026-11-24'],
      previewActions: [{action: 'REMOVE', date: '2026-11-24', name: 'Workout', trainerRoadId: '123', intervalsId: '456'}],
    };
    const workoutClient = {
      getScheduleRequests: () => of([]),
      previewCalendarToCalendar: jasmine.createSpy().and.returnValue(of(preview)),
      copyCalendarToCalendar: jasmine.createSpy().and.returnValue(of(preview)),
    };
    TestBed.configureTestingModule({
      imports: [CopyCalendarToCalendarComponent, NoopAnimationsModule],
      providers: [
        {provide: ConfigurationClient, useValue: {getAllPlatformInfo: () => of({})}},
        {provide: WorkoutClient, useValue: workoutClient},
        {provide: NotificationService, useValue: {success: () => {}}},
        {provide: MatDialog, useValue: {open: () => ({afterClosed: () => of(true)})}},
      ],
    });
    const fixture = TestBed.createComponent(CopyCalendarToCalendarComponent);
    fixture.componentInstance.directions = [{title: 'TrainerRoad to Intervals', value: Platform.DIRECTION_TR_INT}];
    fixture.componentInstance.trainingTypes = [{title: 'Ride', value: 'BIKE'}];
    fixture.detectChanges();
    fixture.componentInstance.formGroup.patchValue({trainerRoadSyncMode: 'MIRROR', allowStaleRemoval: true});

    fixture.componentInstance.today();

    expect(workoutClient.copyCalendarToCalendar).toHaveBeenCalled();
    expect(workoutClient.copyCalendarToCalendar.calls.mostRecent().args[8]).toEqual(preview.previewActions);
    expect(workoutClient.copyCalendarToCalendar.calls.mostRecent().args[9]).toEqual(preview.unidentifiedActivityDates);
    fixture.destroy();
  });

  it('identifies unchanged workouts safely in the sync notification', () => {
    const response = {
      copied: 0, updated: 1, removed: 0, alreadySynced: 1, skipped: 0, filteredOut: 0,
      startDate: '2026-11-24', endDate: '2026-11-24',
      matchedWorkouts: [{date: '2026-11-24', name: '<Ride & recover>', trainerRoadId: '123'}],
      previewActions: [], unidentifiedActivityDates: [],
    };
    const notification = {success: jasmine.createSpy('success')};
    TestBed.configureTestingModule({
      imports: [CopyCalendarToCalendarComponent, NoopAnimationsModule],
      providers: [
        {provide: ConfigurationClient, useValue: {getAllPlatformInfo: () => of({})}},
        {provide: WorkoutClient, useValue: {
          getScheduleRequests: () => of([]),
          previewCalendarToCalendar: () => of(response),
          copyCalendarToCalendar: () => of(response),
        }},
        {provide: NotificationService, useValue: notification},
        {provide: MatDialog, useValue: {open: () => ({afterClosed: () => of(true)})}},
      ],
    });
    const fixture = TestBed.createComponent(CopyCalendarToCalendarComponent);
    fixture.componentInstance.directions = [{title: 'TrainerRoad to Intervals', value: Platform.DIRECTION_TR_INT}];
    fixture.detectChanges();
    fixture.componentInstance.formGroup.patchValue({trainerRoadSyncMode: 'MIRROR', allowStaleRemoval: true});

    fixture.componentInstance.today();

    expect(notification.success).toHaveBeenCalledOnceWith(jasmine.stringMatching(
      /Unchanged matches: 1<br>2026-11-24 - &lt;Ride &amp; recover&gt;/,
    ));
    expect(notification.success.calls.mostRecent().args[0]).not.toContain('TrainerRoad ID');
    fixture.destroy();
  });
});
