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
  trainerRoadTestInProgress = false;
  trainerRoadTestResult: string | null = null;
  trainerRoadTestSucceeded = false;

  constructor(
    private router: Router,
    private formBuilder: FormBuilder,
    private configClient: ConfigurationClient,
    private notificationService: NotificationService
  ) {
  }

  ngOnInit(): void {
    this.inProgress = true
    this.configClient.getConfig().subscribe(config => {
      this.formGroup.patchValue(config.config);
      this.inProgress = false
      this.listenTrainingPeaksCookie()
      this.listenTrainerRoadCookie()
    });
  }

  onSubmit(): void {
    this.inProgress = true
    let newConfiguration = new ConfigData(this.formGroup.getRawValue());

    console.log(newConfiguration)
    this.configClient.updateConfig(newConfiguration).pipe(
      finalize(() => this.inProgress = false)
    ).subscribe(() => {
      this.notificationService.success('Configuration successfully saved')
      this.router.navigate(['/home']);
    });
  }

  testTrainerRoadConnection(): void {
    const control = this.formGroup.controls['trainer-road.auth-cookie'];
    control.markAsTouched();
    if (control.invalid || !control.value) {
      this.trainerRoadTestSucceeded = false;
      this.trainerRoadTestResult = 'Enter a valid SharedTrainerRoadAuth cookie.';
      return;
    }

    this.trainerRoadTestInProgress = true;
    this.trainerRoadTestResult = null;
    this.configClient.testTrainerRoadConnection(control.value).pipe(
      finalize(() => this.trainerRoadTestInProgress = false)
    ).subscribe({
      next: (response) => {
        this.trainerRoadTestSucceeded = true;
        this.trainerRoadTestResult = `Connected successfully — signed in as ${response.username}.`;
        this.notificationService.success(this.trainerRoadTestResult);
      },
      error: (error) => {
        this.trainerRoadTestSucceeded = false;
        const message = error?.error?.message || 'TrainerRoad connection failed.';
        this.trainerRoadTestResult = message;
        this.notificationService.error(message);
      }
    });
  }

  listenTrainerRoadCookie() {
    this.formGroup.controls['trainer-road.auth-cookie'].valueChanges.subscribe(value => {
      this.trainerRoadTestResult = null;
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
