import {ipcMain} from 'electron';
import {bootController} from './boot-controller';

ipcMain.on('boot:healthy', (event) => {
  event.returnValue = bootController?.isHealthy() ?? false;
});

ipcMain.on('app:platform', (event) => {
  event.returnValue = process.platform;
});
