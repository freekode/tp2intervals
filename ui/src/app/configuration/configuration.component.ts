import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ConfigService } from 'infrastructure/config.service';
import { ConfigData } from 'infrastructure/config-data';
import { Router } from '@angular/router';
import { MatCardModule } from "@angular/material/card";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatInputModule } from "@angular/material/input";
import { MatButtonModule } from "@angular/material/button";
import { catchError, EMPTY } from "rxjs";

@Component({
  selector: 'app-configuration',
  standalone: true,
  imports: [ReactiveFormsModule, MatCardModule, MatFormFieldModule, MatInputModule, MatButtonModule],
  templateUrl: './configuration.component.html',
  styleUrl: './configuration.component.scss'
})
export class ConfigurationComponent implements OnInit {

  formGroup: FormGroup = this.formBuilder.group({
    tpAuthCookie: [null, Validators.required],
    athleteId: [null, Validators.required],
    apiKey: [null, Validators.required],
  });

  errorMessage = '';

  constructor(
    private router: Router,
    private formBuilder: FormBuilder,
    private configurationService: ConfigService,
  ) {
  }

  ngOnInit(): void {
    this.configurationService.getConfig().subscribe(config => {
      this.formGroup.setValue({
        tpAuthCookie: config.tpAuthCookie || null,
        athleteId: config.intervalsAthleteId || null,
        apiKey: config.intervalsApiKey || null,
      });
    });
  }

  onSubmit(): void {
    let newConfiguration = new ConfigData(
      this.formGroup.value.tpAuthCookie,
      this.formGroup.value.apiKey,
      this.formGroup.value.athleteId,
    );

    this.configurationService.updateConfig(newConfiguration).pipe(
      catchError(err => {
        this.errorMessage = err.error.error;
        return EMPTY;
      })
    ).subscribe(() => {
      this.router.navigate(['/home']);
    });
  }
}
