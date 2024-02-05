import { Component } from '@angular/core';
import { TrainingPeaksActionsComponent } from "app/training-peaks-actions/training-peaks-actions.component";
import { TrainerRoadActionsComponent } from "app/trainer-road-actions/trainer-road-actions.component";

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [
    TrainingPeaksActionsComponent,
    TrainerRoadActionsComponent
  ],
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss'
})
export class HomeComponent {
}
