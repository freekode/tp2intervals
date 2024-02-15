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
    window.subscriptions.appUpdateAvailable(updateInfo => {
      console.log('Update available', updateInfo)
      if (window.appPlatform === 'darwin') {
        this.notificationService.success(`New version ${updateInfo.version} available for download.\nCheck GitHub page`)
      }
    })

    window.subscriptions.appUpdateDownloaded(updateInfo => {
      console.log('Update downloaded', updateInfo)
      this.notificationService.success(`New version ${updateInfo.version} will be automatically installed.`)
    })
  }
}
