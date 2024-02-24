import { Injectable } from '@angular/core';
import { NotificationService } from "infrastructure/notification.service";


@Injectable({
  providedIn: 'root'
})
export class UpdateService {
  constructor(
    private notificationService: NotificationService
  ) {
  }

  init() {
    if (!window.electron) {
      console.log("Non-electron environment")
      return
    }

    window.electron.subscriptions.appUpdateAvailable(updateInfo => {
      console.log('Update available', updateInfo)
      this.notificationService.success(`New version ${updateInfo.version} is available for download.\nCheck GitHub page`)
    })

    window.electron.subscriptions.appUpdateDownloaded(updateInfo => {
      console.log('Update downloaded', updateInfo)
      this.notificationService.success(`New version ${updateInfo.version} will be automatically installed.`)
    })
  }
}
