import { autoUpdater } from 'electron-updater';
import log from 'electron-log';
import { isDev } from "../environment";
import path from "path";
import { systemEvents } from "../events";
import { BrowserWindow } from "electron";

export class AppUpdater {
  constructor() {
    if (isDev) {
      autoUpdater.updateConfigPath = path.join(process.resourcesPath, 'dev-app-update.yml');
    }
    autoUpdater.logger = log;
    autoUpdater.logger.transports.file.level = isDev ? 'debug' : 'info';

    autoUpdater.on('error', (err) => {
      log.error(`Error in auto-updater. ${err}`);
    });

    autoUpdater.on('update-available', info => {
      systemEvents.emit('update-available', info)
    })

    autoUpdater.on('update-downloaded', event => {
      systemEvents.emit('update-downloaded', event)
    })
  }

  async checkForUpdates() {
    await autoUpdater.checkForUpdatesAndNotify();
  }

  initializeSubscriptions(window: BrowserWindow) {
    systemEvents.on('update-available', (info) => {
      window.webContents.send('app:updateAvailable', info);
    });

    systemEvents.on('update-downloaded', (event) => {
      window.webContents.send('app:updateDownloaded', event);
    });
  }

}

export const appUpdater = new AppUpdater();
