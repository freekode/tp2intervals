import { AfterContentInit, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, RouterOutlet } from '@angular/router';
import { MatCardModule } from "@angular/material/card";
import { MatIconModule } from "@angular/material/icon";
import { MatToolbarModule } from "@angular/material/toolbar";
import { MatButtonModule } from "@angular/material/button";
import { UpdateService } from "infrastructure/update.service";
import { EnvironmentService } from "infrastructure/environment.service";
import { MatOptionModule } from "@angular/material/core";
import { MatProgressBarModule } from "@angular/material/progress-bar";
import { Observable } from "rxjs";
import { ProgressBarService } from "infrastructure/progress-bar.service";


@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule,
    RouterOutlet,
    MatCardModule,
    MatIconModule,
    MatToolbarModule,
    RouterLink,
    MatButtonModule,
    MatOptionModule,
    MatProgressBarModule
  ],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent implements OnInit, AfterContentInit {
  appVersion: string

  menuButtons = [
    {name: 'Home', url: '/home'},
    {name: 'TrainingPeaks', url: '/training-peaks'},
    {name: 'TrainerRoad', url: '/trainer-road'},
    {name: 'Configuration', url: '/config'},
  ]

  progressBarVisible: Observable<boolean>

  constructor(
    protected router: Router,
    private updateService: UpdateService,
    private progressBarService: ProgressBarService,
    private environmentService: EnvironmentService
  ) {
  }

  ngOnInit(): void {
    this.updateService.init()
    this.environmentService.getVersion().subscribe(version => {
      this.appVersion = version
    })
  }

  ngAfterContentInit(): void {
    this.progressBarVisible = this.progressBarService.subscribe()
  }
}
