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

@Component({
  selector: 'app-training-peaks',
  standalone: true,
  imports: [
    TpCopyCalendarToCalendarComponent,
    TpCopyLibraryContainerComponent,
    TpCopyCalendarToLibraryComponent,
    MatExpansionModule,
  ],
  templateUrl: './training-peaks.component.html',
  styleUrl: './training-peaks.component.scss'
})
export class TrainingPeaksComponent implements OnInit {

  constructor() {
  }

  ngOnInit(): void {
  }
}
