import { Injectable } from '@angular/core';
import { MatSnackBar } from "@angular/material/snack-bar";


@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private readonly duration = 10000

  constructor(
    private snackBar: MatSnackBar
  ) {
  }

  success(message) {
    let snackBarRef = this.snackBar.open(message, 'X', {
      duration: this.duration,
      verticalPosition: 'top',
      horizontalPosition: 'right',
      panelClass: 'app-notification-success'
    });
    snackBarRef.onAction().subscribe(() => {
      snackBarRef.dismiss()
    })
  }

  error(message) {
    let snackBarRef = this.snackBar.open(message, 'X', {
      duration: this.duration,
      verticalPosition: 'top',
      horizontalPosition: 'right',
      panelClass: 'app-notification-error'
    });
    snackBarRef.onAction().subscribe(() => {
      snackBarRef.dismiss()
    })
  }
}
