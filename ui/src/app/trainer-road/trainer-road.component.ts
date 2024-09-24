import {Component, OnInit} from '@angular/core';
import {MatProgressBarModule} from "@angular/material/progress-bar";
import {NgIf} from "@angular/common";
import {
  TrCopyLibraryToLibraryComponent
} from "app/trainer-road/tr-copy-library-to-library/tr-copy-library-to-library.component";
import {MatExpansionModule} from "@angular/material/expansion";
import {
  TrCopyCalendarToLibraryComponent
} from "app/trainer-road/tr-copy-calendar-to-library/tr-copy-calendar-to-library.component";
import {Platform} from "infrastructure/platform";
import {ConfigurationClient} from "infrastructure/client/configuration.client";

@Component({
  selector: 'app-trainer-road',
  standalone: true,
  imports: [
    TrCopyLibraryToLibraryComponent,
    MatExpansionModule,
    TrCopyCalendarToLibraryComponent,
    NgIf,
    MatProgressBarModule
  ],
  templateUrl: './trainer-road.component.html',
  styleUrl: './trainer-road.component.scss'
})
export class TrainerRoadComponent implements OnInit {
  platformInfo: any = undefined;

  private readonly platform = Platform.TRAINER_ROAD

  constructor(
    private configurationClient: ConfigurationClient,
  ) {
  }

  ngOnInit(): void {
    this.configurationClient.platformInfo(this.platform.key).subscribe(value => {
      this.platformInfo = value
    })
  }
}
