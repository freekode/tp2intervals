import { Component, OnInit } from '@angular/core';
import {
  TpCopyCalendarToCalendarComponent
} from "app/training-peaks/tp-copy-calendar-to-calendar/tp-copy-calendar-to-calendar.component";
import {
  TpCopyLibraryContainerComponent
} from "app/training-peaks/tp-copy-library-container/tp-copy-library-container.component";
import {
  TpCopyCalendarToLibraryComponent
} from "app/training-peaks/tp-copy-calendar-to-library/tp-copy-calendar-to-library.component";
import { MatExpansionModule } from "@angular/material/expansion";
import { NgIf } from "@angular/common";
import { ConfigurationClient } from "infrastructure/client/configuration.client";
import { Platform } from "infrastructure/platform";
import { MatProgressBarModule } from "@angular/material/progress-bar";
import { MatTooltip, MatTooltipModule } from "@angular/material/tooltip";
import {StravaCopyActivitiesComponent} from "app/strava/strava-copy-activities/strava-copy-activities.component";

@Component({
  selector: 'app-strava',
  standalone: true,
  imports: [
    MatExpansionModule,
    NgIf,
    MatProgressBarModule,
    MatTooltipModule,
    StravaCopyActivitiesComponent,
  ],
  templateUrl: './strava.component.html',
  styleUrl: './strava.component.scss'
})
export class StravaComponent implements OnInit {
  platformInfo: any = undefined;

  private readonly platform = Platform.STRAVA

  constructor(
    private configurationClient: ConfigurationClient
  ) {
  }

  ngOnInit(): void {
    this.configurationClient.platformInfo(this.platform.key).subscribe(value => {
      this.platformInfo = value
    })
  }
}
