import { Component, OnInit } from '@angular/core';
import { Router, RouterLink } from "@angular/router";
import { EnvironmentService } from "infrastructure/environment.service";
import { MatButton, MatButtonModule, MatIconAnchor } from "@angular/material/button";
import { MatToolbar, MatToolbarModule } from "@angular/material/toolbar";

@Component({
  selector: 'app-top-bar',
  standalone: true,
  imports: [
    MatButtonModule,
    MatToolbarModule,
    RouterLink,
  ],
  templateUrl: './top-bar.component.html',
  styleUrl: './top-bar.component.scss'
})
export class TopBarComponent implements OnInit {
  appVersion: string

  menuButtons = [
    {name: 'Home', url: '/home'},
    {name: 'TrainingPeaks', url: '/training-peaks'},
    {name: 'TrainerRoad', url: '/trainer-road'},
    {name: 'Configuration', url: '/config'},
  ]

  constructor(
    protected router: Router,
    private environmentService: EnvironmentService
  ) {
  }

  ngOnInit(): void {
    this.environmentService.getVersion().subscribe(version => {
      this.appVersion = version
    })
  }
}
