import {fakeAsync, TestBed, tick} from '@angular/core/testing';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {delay, of, throwError} from 'rxjs';
import {LibraryClient} from 'infrastructure/client/library-client.service';
import {WorkoutClient} from 'infrastructure/client/workout.client';
import {NotificationService} from 'infrastructure/notification.service';
import {TrCopyLibraryToLibraryComponent} from './tr-copy-library-to-library.component';

describe('TrCopyLibraryToLibraryComponent', () => {
  it('requires a selected workout and recovers after a failed search', fakeAsync(() => {
    const workout = {name: 'Workout', externalData: {trainerRoadId: '123'}};
    const workoutClient = {
      findWorkoutsByName: jasmine.createSpy().and.returnValues(
        throwError(() => new Error('unavailable')),
        of([workout]),
      ),
      copyLibraryToLibrary: jasmine.createSpy().and.returnValue(of({})),
    };
    TestBed.configureTestingModule({
      imports: [TrCopyLibraryToLibraryComponent, NoopAnimationsModule],
      providers: [
        {provide: WorkoutClient, useValue: workoutClient},
        {provide: LibraryClient, useValue: {getLibraries: () => of([]).pipe(delay(1))}},
        {provide: NotificationService, useValue: {success: () => {}}},
      ],
    });
    const fixture = TestBed.createComponent(TrCopyLibraryToLibraryComponent);
    fixture.detectChanges();
    tick(1);
    fixture.detectChanges();
    const component = fixture.componentInstance;
    component.formGroup.controls['intervalsPlan'].setValue({name: 'Library'});
    const control = component.formGroup.controls['trWorkoutDetails'];

    control.setValue('Workout');
    tick(500);
    expect(control.hasError('workoutNotSelected')).toBeTrue();
    expect(component.formGroup.invalid).toBeTrue();
    expect(component.searchFailed).toBeTrue();
    control.setValue('Workout 2');
    tick(500);
    expect(component.searchFailed).toBeFalse();
    expect(workoutClient.findWorkoutsByName).toHaveBeenCalledTimes(2);
    control.setValue(workout);
    tick(500);
    expect(component.formGroup.valid).toBeTrue();
    expect(workoutClient.findWorkoutsByName).toHaveBeenCalledTimes(2);
    component.copyWorkoutSubmit();
    expect(workoutClient.copyLibraryToLibrary).toHaveBeenCalledOnceWith(
      workout.externalData, {name: 'Library'}, jasmine.any(Object),
    );
    fixture.destroy();
  }));
});
