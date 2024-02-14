import { autoUpdater } from 'electron-updater';
import log from 'electron-log';
import { isDev } from "../boot/environment";
import path from "path";

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
  }

  async checkForUpdates() {
    await autoUpdater.checkForUpdatesAndNotify();
  }
}

export const appUpdater = new AppUpdater();
