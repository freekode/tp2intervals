import {TestBed} from '@angular/core/testing';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {SyncPreview, SyncPreviewDialogComponent} from './sync-preview-dialog.component';

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
    expect(text).toContain('TrainerRoad ID 123; Intervals ID 456');
    expect(text).toContain('Marked Intervals events may be removed');
    expect(text).toContain('does not compare every workout detail');
  });
});
