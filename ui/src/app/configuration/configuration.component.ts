import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ConfigService } from 'infrastructure/config.service';
import { ConfigData } from 'infrastructure/config-data';
import { Router } from '@angular/router';
import { MatCardModule } from "@angular/material/card";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatInputModule } from "@angular/material/input";
import { MatButtonModule } from "@angular/material/button";
import { finalize } from "rxjs";
import { MatProgressBarModule } from "@angular/material/progress-bar";
import { NgIf } from "@angular/common";
import { MatSnackBarModule } from "@angular/material/snack-bar";
import { NotificationService } from "infrastructure/notification.service";

@Component({
  selector: 'app-configuration',
  standalone: true,
  imports: [ReactiveFormsModule, MatCardModule, MatFormFieldModule, MatInputModule, MatButtonModule, MatProgressBarModule, MatSnackBarModule, NgIf],
  templateUrl: './configuration.component.html',
  styleUrl: './configuration.component.scss'
})
export class ConfigurationComponent implements OnInit {

  formGroup: FormGroup = this.formBuilder.group({
    'training-peaks.auth-cookie': [null, [Validators.pattern('^Production_tpAuth=.*$')]],
    'trainer-road.auth-cookie': [null, [Validators.pattern('^TrainerRoadAuth=.*$')]],
    'intervals.api-key': [null, Validators.required],
    'intervals.athlete-id': [null, Validators.required],
  });

  inProgress = false;

  constructor(
    private router: Router,
    private formBuilder: FormBuilder,
    private configurationService: ConfigService,
    private notificationService: NotificationService
  ) {
  }

  ngOnInit(): void {
    this.inProgress = true
    this.configurationService.getConfig().subscribe(config => {
      this.formGroup.patchValue(config.config);
      this.inProgress = false
    });
  }

  onSubmit(): void {
    this.inProgress = true
    let newConfiguration = new ConfigData(this.formGroup.getRawValue());

    this.configurationService.updateConfig(newConfiguration).pipe(
      finalize(() => this.inProgress = false)
    ).subscribe(() => {
      this.notificationService.success('Configuration successfully saved')
      this.router.navigate(['/home']);
    });
  }
}
