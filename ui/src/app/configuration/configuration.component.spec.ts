import {FormBuilder} from '@angular/forms';
import {Router} from '@angular/router';
import {of} from 'rxjs';
import {ConfigurationClient} from 'infrastructure/client/configuration.client';
import {ConfigData} from 'infrastructure/config-data';
import {NotificationService} from 'infrastructure/notification.service';
import {ConfigurationComponent} from './configuration.component';

describe('Configuration credential status', () => {
  const intervals = 'intervals.api-key';
  const trainingPeaks = 'training-peaks.auth-cookie';

  function setup(storedCredentials: Record<string, boolean>) {
    const updateConfig = jasmine.createSpy('updateConfig').and.returnValue(of({}));
    const component = new ConfigurationComponent(
      {navigate: jasmine.createSpy('navigate')} as unknown as Router,
      new FormBuilder(),
      {getConfig: () => of(new ConfigData({}, storedCredentials)), updateConfig} as unknown as ConfigurationClient,
      {success: jasmine.createSpy('success')} as unknown as NotificationService,
    );
    component.ngOnInit();
    return {component, updateConfig};
  }

  it('uses credential presence to show stored status and omits blank inputs on save', () => {
    const {component, updateConfig} = setup({[intervals]: true, [trainingPeaks]: true});
    expect(component.storedCredentials[intervals]).toBeTrue();
    expect(component.storedCredentials[trainingPeaks]).toBeTrue();
    expect(component.formGroup.controls[intervals].value).toBeNull();
    expect(component.formGroup.controls[trainingPeaks].value).toBeNull();
    component.formGroup.controls[intervals].setValue('');
    component.formGroup.controls[trainingPeaks].setValue('');
    component.onSubmit();
    const request: ConfigData = updateConfig.calls.mostRecent().args[0];
    expect(Object.keys(request.config)).not.toContain(intervals);
    expect(Object.keys(request.config)).not.toContain(trainingPeaks);
  });

  it('reports missing status when either credential is no longer configured', () => {
    const {component} = setup({[intervals]: false, [trainingPeaks]: false});
    expect(component.storedCredentials[intervals]).toBeFalse();
    expect(component.storedCredentials[trainingPeaks]).toBeFalse();
    expect(component.formGroup.controls[intervals].hasError('required')).toBeTrue();
  });
});
