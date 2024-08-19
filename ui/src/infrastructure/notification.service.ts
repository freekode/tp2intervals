import {Injectable} from '@angular/core';
import {MatSnackBar} from "@angular/material/snack-bar";
import { ToastrService } from 'ngx-toastr';


@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private static readonly duration = 20 * 1000

  constructor(
    private snackBar: MatSnackBar,
    private toastr: ToastrService
  ) {
  }

  success(data) {
    this.toastr.success(data, undefined, {
      enableHtml: true,
      closeButton: true,
      timeOut: NotificationService.duration
    });
  }

  error(data) {
    this.toastr.error(data, undefined, {
      enableHtml: true,
      closeButton: true,
      timeOut: NotificationService.duration
    });
  }
}
