import { Component, OnInit } from '@angular/core';
import { MatGridListModule } from "@angular/material/grid-list";
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from "@angular/forms";
import { MatButtonModule } from "@angular/material/button";
import { MatCardModule } from "@angular/material/card";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatInputModule } from "@angular/material/input";
import { MatProgressBarModule } from "@angular/material/progress-bar";
import { NgIf } from "@angular/common";
import { MatDatepickerModule } from "@angular/material/datepicker";
import { MatNativeDateModule, MatOptionModule } from "@angular/material/core";
import { MatSnackBarModule } from "@angular/material/snack-bar";
import { Router } from "@angular/router";
import { NotificationService } from "infrastructure/notification.service";
import { finalize } from "rxjs";
import { ActivityClient } from "infrastructure/activity.client";
import { MatSelectModule } from "@angular/material/select";
import { formatDate } from "utils/date-formatter";
import { TrCopyLibraryToLibraryComponent } from "app/trainer-road-actions/tr-copy-library-to-library/tr-copy-library-to-library.component";

@Component({
  selector: 'app-trainer-road-actions',
  standalone: true,
  imports: [
    TrCopyLibraryToLibraryComponent
  ],
  templateUrl: './trainer-road-actions.component.html',
  styleUrl: './trainer-road-actions.component.scss'
})
export class TrainerRoadActionsComponent {

}
