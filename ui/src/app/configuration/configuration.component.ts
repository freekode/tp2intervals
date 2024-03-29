import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ConfigData } from 'infrastructure/config-data';
import { Router } from '@angular/router';
import { MatCardModule } from "@angular/material/card";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatInputModule } from "@angular/material/input";
import { MatButtonModule } from "@angular/material/button";
import { finalize, switchMap } from "rxjs";
import { MatProgressBarModule } from "@angular/material/progress-bar";
import { NgIf } from "@angular/common";
import { MatSnackBarModule } from "@angular/material/snack-bar";
import { NotificationService } from "infrastructure/notification.service";
import { MatCheckboxChange, MatCheckboxModule } from "@angular/material/checkbox";
import { ConfigurationClient } from "infrastructure/client/configuration.client";
import { MatOptionModule } from "@angular/material/core";
import { MatSelectModule } from "@angular/material/select";
import { EnvironmentService } from "infrastructure/environment.service";

@Component({
  selector: 'app-configuration',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatProgressBarModule,
    MatSnackBarModule,
    NgIf,
    MatCheckboxModule,
    MatOptionModule,
    MatSelectModule
  ],
  templateUrl: './configuration.component.html',
  styleUrl: './configuration.component.scss'
})
export class ConfigurationComponent implements OnInit {
  private readonly packageName = "org.freekode.tp2intervals";

  formGroup: FormGroup = this.formBuilder.group({
    'training-peaks.auth-cookie': [null, [Validators.pattern('^Production_tpAuth=.*$')]],
    'trainer-road.auth-cookie': [null, [Validators.pattern('^TrainerRoadAuth=.*$')]],
    'intervals.api-key': [null, Validators.required],
    'intervals.athlete-id': [null, Validators.required],
    'intervals.power-range': [null, [Validators.required, Validators.min(0), Validators.max(100)]],
    'intervals.hr-range': [null, [Validators.required, Validators.min(0), Validators.max(100)]],
    'intervals.pace-range': [null, [Validators.required, Validators.min(0), Validators.max(100)]],
  });

  logLevelFormControl = new FormControl<string>('')

  inProgress = false;
  showAdvanced = false;

  constructor(
    private router: Router,
    private formBuilder: FormBuilder,
    private configClient: ConfigurationClient,
    private environmentService: EnvironmentService,
    private notificationService: NotificationService
  ) {
  }

  ngOnInit(): void {
    this.inProgress = true
    this.configClient.getConfig().subscribe(config => {
      this.formGroup.patchValue(config.config);
      this.inProgress = false
    });

    this.environmentService.getLoggerLevel(this.packageName).subscribe(logLevel => {
      this.logLevelFormControl.setValue(logLevel)
    })
  }

  onSubmit(): void {
    this.inProgress = true
    let newConfiguration = new ConfigData(this.formGroup.getRawValue());

    this.environmentService.setLoggerLevel(this.packageName, this.logLevelFormControl.value).pipe(
      switchMap(() => this.configClient.updateConfig(newConfiguration)),
      finalize(() => this.inProgress = false)
    ).subscribe(() => {
      this.notificationService.success('Configuration successfully saved')
      this.router.navigate(['/home']);
    });
  }

  showAdvancedChange($event: MatCheckboxChange) {
    this.showAdvanced = $event.checked
  }
}
