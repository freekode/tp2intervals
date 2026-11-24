import {TestBed} from '@angular/core/testing';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {Router} from '@angular/router';
import {of, throwError} from 'rxjs';
import {ConfigData} from 'infrastructure/config-data';
import {ConfigurationClient} from 'infrastructure/client/configuration.client';
import {NotificationService} from 'infrastructure/notification.service';
import {ConfigurationComponent} from './configuration.component';

describe('ConfigurationComponent', () => {
  it('shows a retry after a load error and enables the form after recovery', () => {
    const client = {
      getConfig: jasmine.createSpy().and.returnValues(
        throwError(() => new Error('unavailable')),
        of(new ConfigData({'intervals.api-key': 'key', 'intervals.athlete-id': 'i123'})),
      ),
    };
    TestBed.configureTestingModule({
      imports: [ConfigurationComponent, NoopAnimationsModule],
      providers: [
        {provide: ConfigurationClient, useValue: client},
        {provide: NotificationService, useValue: {}},
        {provide: Router, useValue: {}},
      ],
    });
    const fixture = TestBed.createComponent(ConfigurationComponent);
    spyOn(fixture.componentInstance, 'listenTrainingPeaksCookie').and.callThrough();
    spyOn(fixture.componentInstance, 'listenTrainerRoadCookie').and.callThrough();
    fixture.detectChanges();

    expect(fixture.componentInstance.inProgress).toBeFalse();
    expect(fixture.nativeElement.querySelector('[role="alert"]').textContent).toContain('Could not load configuration');
    expect(fixture.nativeElement.querySelector('form')).toBeNull();
    fixture.nativeElement.querySelector('[role="alert"] button').click();
    fixture.detectChanges();

    expect(client.getConfig).toHaveBeenCalledTimes(2);
    expect(fixture.componentInstance.listenTrainingPeaksCookie).toHaveBeenCalledTimes(1);
    expect(fixture.componentInstance.listenTrainerRoadCookie).toHaveBeenCalledTimes(1);
    expect(fixture.componentInstance.loadError).toBeFalse();
    expect(fixture.nativeElement.querySelector('form')).not.toBeNull();
    expect(fixture.componentInstance.formGroup.controls['intervals.athlete-id'].value).toBe('i123');
    fixture.destroy();
  });
});
