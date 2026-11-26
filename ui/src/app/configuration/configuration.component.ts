import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {ConfigData} from 'infrastructure/config-data';
import {Router} from '@angular/router';
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatInputModule} from "@angular/material/input";
import {MatButtonModule} from "@angular/material/button";
import {finalize} from "rxjs";
import {MatProgressBarModule} from "@angular/material/progress-bar";
import {NgIf} from "@angular/common";
import {MatSnackBarModule} from "@angular/material/snack-bar";
import {NotificationService} from "infrastructure/notification.service";
import {MatCheckboxModule} from "@angular/material/checkbox";
import {ConfigurationClient} from "infrastructure/client/configuration.client";
import {MatOptionModule} from "@angular/material/core";
import {MatSelectModule} from "@angular/material/select";
import {MatExpansionModule} from "@angular/material/expansion";
import {MatTooltipModule} from "@angular/material/tooltip";

@Component({
  selector: 'app-configuration',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatProgressBarModule,
    MatSnackBarModule,
    NgIf,
    MatCheckboxModule,
    MatOptionModule,
    MatSelectModule,
    MatExpansionModule,
    MatTooltipModule
  ],
  templateUrl: './configuration.component.html',
  styleUrl: './configuration.component.scss'
})
export class ConfigurationComponent implements OnInit {
  formGroup: FormGroup = this.formBuilder.group({
    'intervals.api-key': [null, Validators.required],
    'intervals.athlete-id': [null, Validators.required],
    'intervals.power-range': [null, [Validators.required, Validators.min(0), Validators.max(100)]],
    'intervals.hr-range': [null, [Validators.required, Validators.min(0), Validators.max(100)]],
    'intervals.pace-range': [null, [Validators.required, Validators.min(0), Validators.max(100)]],
    'training-peaks.auth-cookie': [null, [Validators.pattern('^Production_tpAuth=[a-zA-Z0-9-_]*$')]],
    'trainer-road.auth-cookie': [null, [Validators.pattern('^SharedTrainerRoadAuth=.*$')]],
    'trainer-road.remove-html-tags': [null, Validators.required],
    'general.debug-mode': [null, Validators.required],
  });

  inProgress = false;
  loadError = false;
  configLoaded = false;
  trainerRoadTestInProgress = false;
  trainerRoadTestResult: string | null = null;
  trainerRoadTestSucceeded = false;
  trainingPeaksTestInProgress = false;
  trainingPeaksTestResult: string | null = null;
  trainingPeaksTestSucceeded = false;
  private trainerRoadTestRequestId = 0;
  private trainingPeaksTestRequestId = 0;

  constructor(
    private router: Router,
    private formBuilder: FormBuilder,
    private configClient: ConfigurationClient,
    private notificationService: NotificationService
  ) {
  }

  ngOnInit(): void {
    this.listenTrainingPeaksCookie()
    this.listenTrainerRoadCookie()
    this.loadConfig()
  }

  loadConfig(): void {
    this.inProgress = true
    this.loadError = false
    this.configClient.getConfig().pipe(finalize(() => this.inProgress = false)).subscribe({
      next: config => {
        this.formGroup.patchValue(config.config)
        this.configLoaded = true
      },
      error: () => this.loadError = true,
    });
  }

  onSubmit(): void {
    this.inProgress = true
    let newConfiguration = new ConfigData(this.formGroup.getRawValue());

    this.configClient.updateConfig(newConfiguration).pipe(
      finalize(() => this.inProgress = false)
    ).subscribe(() => {
      this.notificationService.success('Configuration successfully saved')
      this.router.navigate(['/home']);
    });
  }

  testTrainerRoadConnection(): void {
    const control = this.formGroup.controls['trainer-road.auth-cookie'];
    const requestId = ++this.trainerRoadTestRequestId
    this.testCookie(
      control,
      (cookie) => this.configClient.testTrainerRoadConnection(cookie),
      (response: any) => `Connection verified — TrainerRoad account: ${response.username}.`,
      () => {
        this.trainerRoadTestInProgress = true
      },
      (value: string, succeeded: boolean) => {
        if (requestId !== this.trainerRoadTestRequestId) return
        this.trainerRoadTestInProgress = false
        this.trainerRoadTestSucceeded = succeeded
        this.trainerRoadTestResult = value
      },
      'Enter a valid SharedTrainerRoadAuth cookie.',
    )
  }

  testTrainingPeaksConnection(): void {
    const control = this.formGroup.controls['training-peaks.auth-cookie'];
    const requestId = ++this.trainingPeaksTestRequestId
    this.testCookie(
      control,
      (cookie) => this.configClient.testTrainingPeaksConnection(cookie),
      (response: any) => response.tokenValid
        ? (response.username
          ? `Connection verified — TrainingPeaks account: ${response.username}.`
          : 'Connection verified — TrainingPeaks token is valid.')
        : 'TrainingPeaks token validation failed.',
      () => {
        this.trainingPeaksTestInProgress = true
      },
      (value: string, succeeded: boolean) => {
        if (requestId !== this.trainingPeaksTestRequestId) return
        this.trainingPeaksTestInProgress = false
        this.trainingPeaksTestSucceeded = succeeded
        this.trainingPeaksTestResult = value
      },
      'Enter a valid Production_tpAuth cookie.',
    )
  }

  private testCookie(
    control: any,
    testRequest: (cookie: string) => any,
    successMessage: (response: any) => string,
    start: () => void,
    finish: (message: string, succeeded: boolean) => void,
    invalidMessage: string,
  ): void {
    control.markAsTouched();
    if (control.invalid || !control.value) {
      finish(invalidMessage, false)
      return
    }
    start()
    const submittedCookie = control.value
    testRequest(submittedCookie).subscribe({
      next: (response: any) => {
        if (control.value !== submittedCookie) return
        const message = successMessage(response)
        finish(message, true)
        this.notificationService.success(message)
      },
      error: (error: any) => {
        if (control.value !== submittedCookie) return
        const message = error?.error?.message || 'Connection failed.'
        finish(message, false)
        this.notificationService.error(message)
      }
    })
  }

  listenTrainerRoadCookie() {
    this.formGroup.controls['trainer-road.auth-cookie'].valueChanges.subscribe(value => {
      ++this.trainerRoadTestRequestId
      this.trainerRoadTestResult = null
      this.trainerRoadTestInProgress = false
      this.trainerRoadTestSucceeded = false
      if (typeof value !== 'string') return
      let split = value.split('SharedTrainerRoadAuth=');
      if (split.length != 2) {
        return
      }
      let splitValue = split[1].split(';')[0]
      let newValue = `SharedTrainerRoadAuth=${splitValue}`
      if (value === newValue) {
        return;
      }
      this.formGroup.controls['trainer-road.auth-cookie'].setValue(newValue)
    })
  }

  listenTrainingPeaksCookie() {
    this.formGroup.controls['training-peaks.auth-cookie'].valueChanges.subscribe(value => {
      ++this.trainingPeaksTestRequestId
      this.trainingPeaksTestResult = null
      this.trainingPeaksTestInProgress = false
      this.trainingPeaksTestSucceeded = false
      if (typeof value !== 'string') return
      let split = value.split('Production_tpAuth=');
      if (split.length != 2) {
        return
      }
      let splitValue = split[1].split(';')[0]
      let newValue = `Production_tpAuth=${splitValue}`
      if (value === newValue) {
        return;
      }
      this.formGroup.controls['training-peaks.auth-cookie'].setValue(newValue)
    })
  }
}
