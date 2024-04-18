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
import { MatTooltip } from "@angular/material/tooltip";

@Component({
  selector: 'app-training-peaks',
  standalone: true,
  imports: [
    TpCopyCalendarToCalendarComponent,
    TpCopyLibraryContainerComponent,
    TpCopyCalendarToLibraryComponent,
    MatExpansionModule,
    NgIf,
    MatProgressBarModule,
    MatTooltip,
  ],
  templateUrl: './training-peaks.component.html',
  styleUrl: './training-peaks.component.scss'
})
export class TrainingPeaksComponent implements OnInit {
  platformInfo: any = undefined;

  private readonly platform = Platform.TRAINING_PEAKS

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
