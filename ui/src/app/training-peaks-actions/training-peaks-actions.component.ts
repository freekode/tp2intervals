import { Component, OnInit } from '@angular/core';
import { TpPlanWorkoutsComponent } from "app/training-peaks-actions/tp-plan-workouts/tp-plan-workouts.component";
import { TpCopyPlanComponent } from "app/training-peaks-actions/tp-copy-plan/tp-copy-plan.component";
import { TpCopyPlannedWorkoutsComponent } from "app/training-peaks-actions/tp-copy-planned-workouts/tp-copy-planned-workouts.component";
import { TpCopyWorkoutsComponent } from "app/training-peaks-actions/tp-copy-workouts/tp-copy-workouts.component";

@Component({
  selector: 'app-training-peaks-actions',
  standalone: true,
  imports: [
    TpPlanWorkoutsComponent,
    TpCopyPlanComponent,
    TpCopyPlannedWorkoutsComponent,
    TpCopyWorkoutsComponent
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
