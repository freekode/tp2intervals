import { Component, OnInit } from '@angular/core';
import {
  TpCopyPlannedWorkoutsComponent
} from "app/training-peaks-actions/tp-copy-planned-workouts/tp-copy-planned-workouts.component";
import {
  TpCopyLibraryItemComponent
} from "app/training-peaks-actions/tp-copy-library-item/tp-copy-library-item.component";
import {
  ToCopyPlannedToLibraryComponent
} from "app/training-peaks-actions/to-copy-planned-to-library/to-copy-planned-to-library.component";

@Component({
  selector: 'app-training-peaks-actions',
  standalone: true,
  imports: [
    TpCopyPlannedWorkoutsComponent,
    TpCopyLibraryItemComponent,
    ToCopyPlannedToLibraryComponent,
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
