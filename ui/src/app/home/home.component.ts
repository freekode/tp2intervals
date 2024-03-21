import { Component } from '@angular/core';
import { TrainingPeaksComponent } from "app/training-peaks/training-peaks.component";
import { TrainerRoadComponent } from "app/trainer-road/trainer-road.component";

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [
    TrainingPeaksComponent,
    TrainerRoadComponent
  ],
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss'
})
export class HomeComponent {
}
