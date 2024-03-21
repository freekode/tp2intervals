import { Routes } from '@angular/router';
import { HomeComponent } from "app/home/home.component";
import { ConfigurationComponent } from "app/configuration/configuration.component";
import { canActivateHome } from "app/home/can-activate-home";
import { TrainingPeaksComponent } from "app/training-peaks/training-peaks.component";
import { TrainerRoadComponent } from "app/trainer-road/trainer-road.component";

export const routes: Routes = [
  {
    path: 'home',
    component: HomeComponent,
    canActivate: [canActivateHome]
  },
  {
    path: 'training-peaks',
    component: TrainingPeaksComponent,
    canActivate: [canActivateHome]
  },
  {
    path: 'trainer-road',
    component: TrainerRoadComponent,
    canActivate: [canActivateHome]
  },
  {
    path: 'config',
    component: ConfigurationComponent,
  },
  {
    path: '',
    redirectTo: '/home',
    pathMatch: 'full'
  }
];
