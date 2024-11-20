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
    'training-peaks.auth-cookie': [null, [Validators.pattern('^Production_tpAuth=[a-zA-Z0-9-_]*$')]],
    'trainer-road.auth-cookie': [null, [Validators.pattern('^SharedTrainerRoadAuth=.*$')]],
    'trainer-road.remove-html-tags': [null, Validators.required],
    'intervals.api-key': [null, Validators.required],
    'intervals.athlete-id': [null, Validators.required],
    'intervals.power-range': [null, [Validators.required, Validators.min(0), Validators.max(100)]],
    'intervals.hr-range': [null, [Validators.required, Validators.min(0), Validators.max(100)]],
    'intervals.pace-range': [null, [Validators.required, Validators.min(0), Validators.max(100)]],
    'generic.log-level': [null, [Validators.required]],
  });

  inProgress = false;

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
}
