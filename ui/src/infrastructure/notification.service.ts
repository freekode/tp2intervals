import { Injectable } from '@angular/core';
import { MatSnackBar } from "@angular/material/snack-bar";


@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  constructor(
    private snackBar: MatSnackBar
  ) {
  }

  error(message) {
    this.snackBar.open(message, '', {
      duration: 4000,
      horizontalPosition: 'end',
      verticalPosition: 'top',
      panelClass: 'app-notification-error'
    });
  }

  success(message) {
    this.snackBar.open(message, '', {
      duration: 4000,
      horizontalPosition: 'end',
      verticalPosition: 'top',
      panelClass: 'app-notification-success'
    });
  }
}
