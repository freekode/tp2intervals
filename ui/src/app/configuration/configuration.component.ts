import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ConfigService } from 'infrastructure/config.service';
import { ConfigData } from 'infrastructure/config-data';
import { Router } from '@angular/router';
import { MatCardModule } from "@angular/material/card";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatInputModule } from "@angular/material/input";
import { MatButtonModule } from "@angular/material/button";
import { catchError, EMPTY, finalize } from "rxjs";
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
    tpAuthCookie: [null, [Validators.pattern('^Production_tpAuth=.*$')]],
    trAuthCookie: [null, [Validators.pattern('^TrainerRoadAuth=.*$')]],
    athleteId: [null, Validators.required],
    apiKey: [null, Validators.required],
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
      this.formGroup.setValue({
        tpAuthCookie: config.tpAuthCookie || null,
        athleteId: config.intervalsAthleteId || null,
        apiKey: config.intervalsApiKey || null,
      });
      this.inProgress = false
    });
  }

  onSubmit(): void {
    this.inProgress = true
    let newConfiguration = new ConfigData(
      this.formGroup.value.tpAuthCookie,
      this.formGroup.value.apiKey,
      this.formGroup.value.athleteId,
    );

    this.configurationService.updateConfig(newConfiguration).pipe(
      finalize(() => this.inProgress = false)
    ).subscribe(() => {
      this.notificationService.success('Configuration successfully saved')
      this.router.navigate(['/home']);
    });
  }
}
