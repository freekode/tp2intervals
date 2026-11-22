import {of} from 'rxjs';
import {FormBuilder} from '@angular/forms';
import {MatDialog} from '@angular/material/dialog';
import {CopyCalendarToCalendarComponent} from './copy-calendar-to-calendar.component';
import {TrainingPeaksSyncPreview, WorkoutClient} from 'infrastructure/client/workout.client';
import {ConfigurationClient} from 'infrastructure/client/configuration.client';
import {NotificationService} from 'infrastructure/notification.service';
import {Platform} from 'infrastructure/platform';

describe('TrainingPeaks legacy review', () => {
  const held: TrainingPeaksSyncPreview = {
    sourceWorkoutId: 'new', sourceDate: '2026-11-22', status: 'BROKEN_OR_UNVERIFIED_LINK',
    reason: 'PREVIOUS_REVIEW_HOLD', candidates: [], canResolveUnmatchedHold: true,
  };
  const historical: TrainingPeaksSyncPreview[] = [1, 2].map(day => ({
    sourceWorkoutId: `old-${day}`, status: 'SINGLE_LEGACY_CANDIDATE',
    reason: 'LEGACY_ACCOUNT_UNVERIFIED', candidates: [{eventId: day, date: `2026-11-0${day}`}],
  }));

  function setup() {
    const resolve = jasmine.createSpy('resolveTrainingPeaksHold').and.returnValue(of({eventId: 71}));
    const repair = jasmine.createSpy('repairTrainingPeaksLink').and.returnValue(of({eventId: 72, recreated: false}));
    const copy = jasmine.createSpy('copyCalendarToCalendar').and.returnValue(of({copied: 1}));
    const schedule = jasmine.createSpy('scheduleCopyCalendarToCalendar').and.returnValue(of({}));
    const open = jasmine.createSpy('open').and.returnValue({afterClosed: () => of(true)});
    const component = new CopyCalendarToCalendarComponent(
      new FormBuilder(), {} as ConfigurationClient,
      {resolveTrainingPeaksHold: resolve, repairTrainingPeaksLink: repair, copyCalendarToCalendar: copy,
        scheduleCopyCalendarToCalendar: schedule, getScheduleRequests: () => of([])} as unknown as WorkoutClient,
      {success: () => {}} as unknown as NotificationService, {open} as unknown as MatDialog,
    );
    component.previewResult = {
      selection: {startDate: '2026-11-21', endDate: '2026-11-22', types: ['BIKE']},
      inventory: {
        revision: 'reviewed', sourceAccountId: 'source', destinationAthleteId: 'destination',
        workouts: [...historical, held], previewOnly: false, activationEnabled: false,
      },
    };
    return {component, resolve, repair, copy, open, schedule};
  }

  it('groups and sorts historical candidates and bulk-selects the visible filter', () => {
    const {component} = setup();
    expect(component.visibleInventoryGroups[0].rows).toEqual([held]);
    expect(component.visibleInventoryGroups[1].rows).toEqual([...historical].reverse());
    expect(component.classificationCounts.find(item => item.status === historical[0].status)?.count).toBe(2);
    component.classification = held.status;
    component.selectVisibleLegacyLinks(true);
    expect(component.selectedLegacyLinks.size).toBe(0);
    component.classification = historical[0].status;
    component.selectVisibleLegacyLinks(true);
    expect([...component.selectedLegacyLinks.keys()]).toEqual(['old-2', 'old-1']);
    component.selectVisibleLegacyLinks(false);
    expect(component.selectedLegacyLinks.size).toBe(0);
  });

  it('always sends skip synced for forward copy and preserves the reverse preference', () => {
    const {component} = setup();
    component.formGroup = new FormBuilder().group({
      direction: {sourcePlatform: Platform.TRAINING_PEAKS.key, targetPlatform: Platform.INTERVALS.key},
      skipSynced: false,
    });
    expect(component.effectiveSkipSynced).toBeTrue();
    component.formGroup.controls['direction'].setValue({
      sourcePlatform: Platform.INTERVALS.key, targetPlatform: Platform.TRAINING_PEAKS.key,
    });
    expect(component.effectiveSkipSynced).toBeFalse();
  });

  it('submits only the individually acknowledged hold with the reviewed revision', () => {
    const {component, resolve} = setup();
    const token = {value: 'synthetic-token'} as HTMLInputElement;
    component.resolveHold(held, token);
    expect(resolve).not.toHaveBeenCalled();
    component.acknowledgedHold = 'new';
    const {selection, inventory} = component.previewResult!;
    inventory.previewOnly = true;
    component.resolveHold(held, token);
    expect(resolve).not.toHaveBeenCalled();
    inventory.previewOnly = false;
    component.resolveHold(held, token);
    expect(resolve).toHaveBeenCalledOnceWith(selection, 'reviewed', 'new', 'synthetic-token');
    expect(token.value).toBe('');
    expect(component.previewResult).toBeUndefined();
  });

  it('requires a destination-specific confirmation before manual synchronization', () => {
    const {component, copy, open} = setup();
    component.formGroup = new FormBuilder().group({
      direction: Platform.DIRECTION_TP_INT,
      trainingTypes: [['BIKE']],
      skipSynced: true,
      startDate: new Date(2026, 10, 22),
      endDate: new Date(2026, 10, 22),
    });
    open.and.returnValue({afterClosed: () => of(false)});
    component.submit();
    expect(copy).not.toHaveBeenCalled();
    expect(open.calls.mostRecent().args[1].data).toEqual(jasmine.objectContaining({
      action: 'Synchronize workouts', destination: 'Intervals.icu',
    }));
    open.and.returnValue({afterClosed: () => of(true)});
    component.submit();
    expect(copy).toHaveBeenCalledOnceWith('2026-11-22', '2026-11-22', ['BIKE'], true, Platform.DIRECTION_TP_INT);
  });

  it('names the workout and exact event IDs before allowing a repair', () => {
    const {component, repair, open} = setup();
    const missing: TrainingPeaksSyncPreview = {
      ...held, reason: 'LINK_MISSING',
      missingLinkRepair: {oldEventId: 71, replacement: {eventId: 72}},
    };
    const token = {value: 'synthetic-token'} as HTMLInputElement;
    open.and.returnValue({afterClosed: () => of(false)});
    component.repairLink(missing, token);
    expect(repair).not.toHaveBeenCalled();
    const data = open.calls.mostRecent().args[1].data;
    expect(data.destination).toBe('Intervals.icu athlete destination');
    expect(data.details).toContain('missing event 71 to existing event 72');
    expect(data.details).toContain('workout new');
    const selection = component.previewResult!.selection;
    open.and.returnValue({afterClosed: () => of(true)});
    component.repairLink(missing, token);
    expect(repair).toHaveBeenCalledOnceWith(selection, 'reviewed', 'new', 71, 72, false, 'synthetic-token');
    expect(token.value).toBe('');
  });


  it('stages an explicit rolling request with configured cadence, timezone and pause guidance', () => {
    const {component, schedule, open} = setup();
    component.formGroup = new FormBuilder().group({
      direction: Platform.DIRECTION_TP_INT, trainingTypes: [['BIKE']], skipSynced: false,
    });
    component.scheduleSettings = {
      intervalMinutes: 20, timezone: 'Europe/Amsterdam', forwardEnabled: false, previewOnly: false,
      otherIntervalMinutes: 3, otherTimezone: 'UTC',
    };
    open.and.returnValue({afterClosed: () => of(false)});
    component.scheduleToday('TODAY_AND_TOMORROW');
    expect(schedule).not.toHaveBeenCalled();
    const details = open.calls.mostRecent().args[1].data.details;
    expect(details).toContain('Today + tomorrow (rolling) · every 20 minutes · Europe/Amsterdam');
    expect(details).toContain('paused');
    expect(details).toContain('without starting a sync');

    open.and.returnValue({afterClosed: () => of(true)});
    component.scheduleToday('TODAY_AND_TOMORROW');
    expect(schedule).toHaveBeenCalledOnceWith(['BIKE'], true, Platform.DIRECTION_TP_INT, 'TODAY_AND_TOMORROW');
    component.scheduleSettings.previewOnly = true;
    component.scheduleToday('TODAY_AND_TOMORROW');
    expect(schedule.calls.count()).toBe(1);
  });

});
