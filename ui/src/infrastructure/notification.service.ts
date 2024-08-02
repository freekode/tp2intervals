import {Injectable} from '@angular/core';
import {MatSnackBar} from "@angular/material/snack-bar";
import {NotificationComponent} from "app/notification/notification.component";


@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private static readonly duration = 20 * 1000

  constructor(
    private snackBar: MatSnackBar
  ) {
  }

  success(data) {
    this.snackBar.openFromComponent(NotificationComponent, {
      verticalPosition: 'top',
      horizontalPosition: 'right',
      panelClass: 'app-notification-success',
      data
    });
  }

  error(data) {
    this.snackBar.openFromComponent(NotificationComponent, {
      verticalPosition: 'top',
      horizontalPosition: 'right',
      panelClass: 'app-notification-error',
      data
    });
    // let snackBarRef = this.snackBar.open(message, 'X', {
    //   // duration: duration,
    //   verticalPosition: 'top',
    //   horizontalPosition: 'right',
    //   panelClass: 'app-notification-error'
    // });
    // snackBarRef.onAction().subscribe(() => {
    //   snackBarRef.dismiss()
    // })
  }
}
