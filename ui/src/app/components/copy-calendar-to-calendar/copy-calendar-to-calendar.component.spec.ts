import {of} from 'rxjs';
import {FormBuilder} from '@angular/forms';
import {CopyCalendarToCalendarComponent} from './copy-calendar-to-calendar.component';
import {TrainingPeaksSyncPreview, WorkoutClient} from 'infrastructure/client/workout.client';
import {ConfigurationClient} from 'infrastructure/client/configuration.client';
import {NotificationService} from 'infrastructure/notification.service';

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
    const component = new CopyCalendarToCalendarComponent(
      new FormBuilder(), {} as ConfigurationClient,
      {resolveTrainingPeaksHold: resolve} as unknown as WorkoutClient, {} as NotificationService,
    );
    component.previewResult = {
      selection: {startDate: '2026-11-21', endDate: '2026-11-22', types: ['BIKE']},
      inventory: {
        revision: 'reviewed', sourceAccountId: 'source', destinationAthleteId: 'destination',
        workouts: [...historical, held], previewOnly: false, activationEnabled: false,
      },
    };
    return {component, resolve};
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
});
