import {Component, OnInit} from '@angular/core';
import {MatGridListModule} from "@angular/material/grid-list";
import {AbstractControl, FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, ValidationErrors, Validators} from "@angular/forms";
import {MatButtonModule} from "@angular/material/button";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatInputModule} from "@angular/material/input";
import {MatProgressBarModule} from "@angular/material/progress-bar";
import {AsyncPipe, NgIf} from "@angular/common";
import {MatDatepickerModule} from "@angular/material/datepicker";
import {MatNativeDateModule} from "@angular/material/core";
import {MatSnackBarModule} from "@angular/material/snack-bar";
import {MatSelectModule} from "@angular/material/select";
import {MatCheckboxModule} from "@angular/material/checkbox";
import {WorkoutClient} from "infrastructure/client/workout.client";
import {NotificationService} from "infrastructure/notification.service";
import {catchError, debounceTime, finalize, map, Observable, of, shareReplay, switchMap} from "rxjs";
import {LibraryClient} from "infrastructure/client/library-client.service";
import {Platform} from "infrastructure/platform";
import {MatAutocompleteModule} from "@angular/material/autocomplete";

function selectedWorkout(control: AbstractControl): ValidationErrors | null {
  const value: unknown = control.value
  if (!value) return null
  if (typeof value !== 'object') return {workoutNotSelected: true}
  const workout = value as {externalData?: {trainerRoadId?: unknown}}
  return typeof workout.externalData?.trainerRoadId === 'string' && workout.externalData.trainerRoadId.trim()
    ? null : {workoutNotSelected: true}
}

@Component({
  selector: 'tr-copy-library-to-library',
  standalone: true,
  imports: [
    MatGridListModule,
    FormsModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    ReactiveFormsModule,
    MatProgressBarModule,
    NgIf,
    MatDatepickerModule,
    MatNativeDateModule,
    MatSnackBarModule,
    MatSelectModule,
    MatCheckboxModule,
    AsyncPipe,
    MatAutocompleteModule
  ],
  templateUrl: './tr-copy-library-to-library.component.html',
  styleUrl: './tr-copy-library-to-library.component.scss'
})
export class TrCopyLibraryToLibraryComponent implements OnInit {

  formGroup: FormGroup = this.formBuilder.group({
    trWorkoutDetails: [null, [Validators.required, selectedWorkout]],
    intervalsPlan: [null, Validators.required],
  });

  searchInProgress = false
  searchFailed = false
  submitInProgress = false

  get canSearch(): boolean {
    const value: unknown = this.formGroup.controls['trWorkoutDetails'].value
    return typeof value === 'string' && value.trim().length >= 3
  }

  workouts: Observable<any[]>;
  intervalsLibraryItem: Observable<{ name: any; value: any }[]>;

  private readonly direction = Platform.DIRECTION_TR_INT

  constructor(
    private formBuilder: FormBuilder,
    private workoutClient: WorkoutClient,
    private planClient: LibraryClient,
    private notificationService: NotificationService
  ) {
  }

  ngOnInit(): void {
    this.loadPlans();
    this.subscribeOnWorkoutNameChange();
  }

  copyWorkoutSubmit() {
    this.submitInProgress = true
    let workoutDetails = this.formGroup.value.trWorkoutDetails
    let intervalsPlan = this.formGroup.value.intervalsPlan
    this.workoutClient.copyLibraryToLibrary(workoutDetails.externalData, intervalsPlan, this.direction).pipe(
      finalize(() => this.submitInProgress = false)
    ).subscribe((response) => {
      this.notificationService.success(`Copied successfully`)
    })
  }

  getWorkoutDetailsName(details) {
    if (!details) {
      return ''
    }
    if (typeof details === 'string') return details
    return `${details.name} (Duration: ${details.duration || '0'}, Load: ${details.load})`
  }

  private loadPlans() {
    this.formGroup.disable()
    this.intervalsLibraryItem = this.planClient.getLibraries(Platform.INTERVALS.key).pipe(
      map(plans => plans.map(plan => {
          return {name: plan.name, value: plan}
        })
      ),
      finalize(() => {
        this.formGroup.enable()
      })
    )
  }

  private subscribeOnWorkoutNameChange() {
    this.workouts = this.formGroup.controls['trWorkoutDetails'].valueChanges.pipe(
      debounceTime(500),
      switchMap(value => {
        this.searchFailed = false
        if (typeof value !== 'string' || value.trim().length < 3) return of([])
        this.searchInProgress = true
        return this.workoutClient.findWorkoutsByName(Platform.TRAINER_ROAD.key, value).pipe(
          catchError(() => {
            this.searchFailed = true
            return of([])
          }),
          finalize(() => {
            this.searchInProgress = false
          })
        )
      }),
      shareReplay({bufferSize: 1, refCount: true}),
    )
  }
}
