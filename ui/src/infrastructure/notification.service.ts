import {Injectable} from '@angular/core';
import {ToastrService} from 'ngx-toastr';


@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private static readonly duration = 20 * 1000

  constructor(
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
