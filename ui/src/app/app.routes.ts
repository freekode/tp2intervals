import { Routes } from '@angular/router';
import { HomeComponent } from "app/home/home.component";
import { ConfigurationComponent } from "app/configuration/configuration.component";
import { canActivateHome } from "app/home/can-activate-home";

export const routes: Routes = [
  {
    path: 'home',
    component: HomeComponent,
    canActivate: [canActivateHome]
  },
  {
    path: 'configuration',
    component: ConfigurationComponent,
  },
  {
    path: '',
    redirectTo: '/home',
    pathMatch: 'full'
  }
];
