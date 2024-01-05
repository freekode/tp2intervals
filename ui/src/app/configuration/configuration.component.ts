import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ConfigService } from 'infrastructure/config.service';
import { ConfigData } from 'infrastructure/config-data';
import { Router } from '@angular/router';
import { MatCardModule } from "@angular/material/card";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatInputModule } from "@angular/material/input";

@Component({
  selector: 'app-configuration',
  standalone: true,
  imports: [ReactiveFormsModule, MatCardModule, MatFormFieldModule, MatInputModule],
  templateUrl: './configuration.component.html',
  styleUrl: './configuration.component.scss'
})
export class ConfigurationComponent implements OnInit {

  formGroup: FormGroup = this.getConfigurationForm();

  errorMessage = '';

  constructor(
    private router: Router,
    private formBuilder: FormBuilder,
    private configurationService: ConfigService,
  ) {
  }

  ngOnInit(): void {
    let configuration = this.configurationService.getConfig();

    // this.formGroup.setValue({
    //   athleteId: configuration.athleteId || null,
    //   apiKey: configuration.apiKey || null,
    // });
  }

  onSubmit(): void {
    let newConfiguration = new ConfigData(
      this.formGroup.value.tpAuthCookie,
      this.formGroup.value.athleteId,
      this.formGroup.value.apiKey
    );

    this.configurationService.updateConfig(newConfiguration).subscribe(() => {
      this.router.navigate(['/home']);
    });
  }

  private getConfigurationForm(): FormGroup {
    return this.formBuilder.group({
      athleteId: [null, Validators.required],
      apiKey: [null, Validators.required],
    });
  }
}
