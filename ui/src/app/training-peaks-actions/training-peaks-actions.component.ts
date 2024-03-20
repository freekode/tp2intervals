import { Component, OnInit } from '@angular/core';
import {
  TpCopyCalendarToCalendarComponent
} from "app/training-peaks-actions/tp-copy-calendar-to-calendar/tp-copy-calendar-to-calendar.component";
import {
  TpCopyLibraryContainerComponent
} from "app/training-peaks-actions/tp-copy-library-container/tp-copy-library-container.component";
import {
  TpCopyCalendarToLibraryComponent
} from "app/training-peaks-actions/tp-copy-calendar-to-library/tp-copy-calendar-to-library.component";

@Component({
  selector: 'app-training-peaks-actions',
  standalone: true,
  imports: [
    TpCopyCalendarToCalendarComponent,
    TpCopyLibraryContainerComponent,
    TpCopyCalendarToLibraryComponent,
  ],
  templateUrl: './training-peaks-actions.component.html',
  styleUrl: './training-peaks-actions.component.scss'
})
export class TrainingPeaksActionsComponent implements OnInit {

  constructor() {
  }

  ngOnInit(): void {
  }
}
