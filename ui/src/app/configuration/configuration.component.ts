import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ConfigurationService } from 'infrastructure/configuration.service';
import { ConfigurationData } from 'infrastructure/configuration-data';
import { Router } from '@angular/router';
import { IntervalsClient } from 'infrastructure/intervals.client';
import { catchError, EMPTY } from 'rxjs';


@Component({
  selector: 'app-configuration',
  templateUrl: './configuration.component.html',
  styleUrls: ['./configuration.component.scss']
})
export class ConfigurationComponent implements OnInit {

  formGroup: FormGroup = this.getConfigurationForm();

  errorMessage = '';

  constructor(
    private router: Router,
    private formBuilder: FormBuilder,
    private configurationService: ConfigurationService,
    private intervalsClient: IntervalsClient,
  ) {
  }

  ngOnInit(): void {
    let configuration = this.configurationService.getConfiguration();

    this.formGroup.setValue({
      athleteId: configuration.athleteId || null,
      apiKey: configuration.apiKey || null,
    });
  }

  onSubmit(): void {
    let newConfiguration = new ConfigurationData(
      this.formGroup.value.athleteId,
      this.formGroup.value.apiKey
    );

    this.configurationService.setConfiguration(newConfiguration);
    this.intervalsClient.test(newConfiguration.athleteId!).pipe(
      catchError(err => {
        if (err.status === 403) {
          this.errorMessage = 'Wrong credentials';
        }
        return EMPTY;
      })
    ).subscribe(() => {
      this.errorMessage = '';
      this.router.navigate(['/wellness']);
    });
  }

  private getConfigurationForm(): FormGroup {
    return this.formBuilder.group({
      athleteId: [null, Validators.required],
      apiKey: [null, Validators.required],
    });
  }
}
