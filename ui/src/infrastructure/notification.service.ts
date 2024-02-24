import { Injectable } from '@angular/core';
import { MatSnackBar } from "@angular/material/snack-bar";


@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private readonly duration = 5000

  constructor(
    private snackBar: MatSnackBar
  ) {
  }

  error(message) {
    this.snackBar.open(message, '', {
      duration: this.duration,
      verticalPosition: 'top',
      horizontalPosition: 'right',
      panelClass: 'app-notification-error'
    });
  }

  success(message) {
    this.snackBar.open(message, '', {
      duration: this.duration,
      verticalPosition: 'top',
      horizontalPosition: 'right',
      panelClass: 'app-notification-success'
    });
  }
}
