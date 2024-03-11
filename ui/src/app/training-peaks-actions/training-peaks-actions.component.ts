import { Component, OnInit } from '@angular/core';
import {
  CopyPlannedWorkoutsToTpComponent
} from "app/training-peaks-actions/copy-planned-workouts-to-tp/copy-planned-workouts-to-tp.component";
import {
  TpCopyLibraryItemComponent
} from "app/training-peaks-actions/tp-copy-library-item/tp-copy-library-item.component";
import {
  CopyPlannedWorkoutsFromTpComponent
} from "app/training-peaks-actions/copy-planned-workouts-from-tp/copy-planned-workouts-from-tp.component";

@Component({
  selector: 'app-training-peaks-actions',
  standalone: true,
  imports: [
    CopyPlannedWorkoutsToTpComponent,
    TpCopyLibraryItemComponent,
    CopyPlannedWorkoutsFromTpComponent,
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
