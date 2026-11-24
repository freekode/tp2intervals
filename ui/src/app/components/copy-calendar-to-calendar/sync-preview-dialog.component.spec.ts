import {TestBed} from '@angular/core/testing';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {describeSkippedWorkout, SyncPreview, SyncPreviewDialogComponent} from './sync-preview-dialog.component';

describe('SyncPreviewDialogComponent', () => {
  const preview: SyncPreview = {
    trainerRoadSyncMode: 'ADD_MISSING',
    startDate: '2026-11-24',
    endDate: '2026-11-30',
    copied: 0,
    updated: 0,
    removed: 0,
    alreadySynced: 1,
    skipped: 0,
    filteredOut: 1,
    unidentifiedActivityDates: ['2026-11-26'],
    matchedWorkouts: [{date: '2026-11-24', name: 'Endurance', trainerRoadId: '123'}],
    previewActions: [],
  };

  function render(data: SyncPreview): string {
    TestBed.configureTestingModule({
      imports: [SyncPreviewDialogComponent],
      providers: [
        {provide: MAT_DIALOG_DATA, useValue: data},
        {provide: MatDialogRef, useValue: {close: () => {}}},
      ],
    });
    const fixture = TestBed.createComponent(SyncPreviewDialogComponent);
    fixture.detectChanges();
    return fixture.nativeElement.textContent.replace(/\s+/g, ' ');
  }

  it('explains that add-only matches are not checked and does not warn about removal', () => {
    const text = render(preview);
    expect(text).toContain('No changes planned.');
    expect(text).toContain('Existing matches (not checked for changes): 1');
    expect(text).toContain('2026-11-24 - Endurance (TrainerRoad ID 123)');
    expect(text).toContain('activities without workout IDs');
    expect(text).not.toContain('may be removed');
    expect(text).not.toContain('Your selected workouts are already');
  });

  it('warns about unidentified activities in mirror mode and identifies the affected event', () => {
    const text = render({
      ...preview,
      trainerRoadSyncMode: 'MIRROR',
      updated: 1,
      previewActions: [{action: 'UPDATE', date: '2026-11-24', name: 'Workout', trainerRoadId: '123', intervalsId: '456'}],
    });
    expect(text).toContain('Unchanged matches: 1');
    expect(text).toContain('2026-11-24 - Endurance (TrainerRoad ID 123)');
    expect(text).toContain('UPDATE: 2026-11-24 - Workout (TrainerRoad ID 123)');
    expect(text).not.toContain('Intervals ID 456');
    expect(text).toContain('Marked destination workouts may be removed');
    expect(text).toContain('does not compare every workout detail');
  });

  it('shows only TrainerRoad IDs for added and removed workouts', () => {
    const text = render({
      ...preview,
      trainerRoadSyncMode: 'MIRROR',
      copied: 1,
      removed: 1,
      previewActions: [
        {action: 'ADD', date: '2026-11-26', name: 'New workout', trainerRoadId: '321', intervalsId: null},
        {action: 'REMOVE', date: '2026-11-28', name: 'Old workout', trainerRoadId: '654', intervalsId: '987'},
      ],
    });
    expect(text).toContain('ADD: 2026-11-26 - New workout (TrainerRoad ID 321)');
    expect(text).toContain('REMOVE: 2026-11-28 - Old workout (TrainerRoad ID 654)');
    expect(text).not.toContain('987');
  });

  it('shows only the TrainerRoad ID for skipped workouts', () => {
    const text = describeSkippedWorkout({date: '2026-11-28', name: 'Workout', trainerRoadId: '123',
      intervalsIds: ['456'], trainingPeaksIds: ['789'], reason: 'PROTECTED_EVENT'});
    expect(text).toContain('TrainerRoad ID: 123');
    expect(text).not.toContain('456');
    expect(text).not.toContain('789');
  });
});
