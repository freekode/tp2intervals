import { Component, OnInit } from '@angular/core';
import {
  TpScheduleWorkoutsComponent
} from "app/training-peaks-actions/tp-schedule-workouts/tp-schedule-workouts.component";
import {
  TpCopyLibraryItemComponent
} from "app/training-peaks-actions/tp-copy-library-item/tp-copy-library-item.component";
import {
  TpCopyPlannedWorkoutsComponent
} from "app/training-peaks-actions/tp-copy-planned-workouts/tp-copy-planned-workouts.component";

@Component({
  selector: 'app-training-peaks-actions',
  standalone: true,
  imports: [
    TpScheduleWorkoutsComponent,
    TpCopyLibraryItemComponent,
    TpCopyPlannedWorkoutsComponent,
  ],
  templateUrl: './training-peaks-actions.component.html',
  styleUrl: './training-peaks-actions.component.scss'
})
export class TrainingPeaksActionsComponent implements OnInit {

  constructor() {
  }

  ngOnInit(): void {
  }
}
