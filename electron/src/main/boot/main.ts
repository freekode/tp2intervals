import { app, ipcMain } from 'electron';
import { bootController, getBootController } from './boot-controller';

ipcMain.on('boot:address', (event) => {
  event.returnValue = bootController?.getProcessAddress();
});

ipcMain.on('boot:healthy', (event) => {
  event.returnValue = bootController?.isHealthy() ?? false;
});

ipcMain.on('app:version', (event) => {
  event.returnValue = app.getVersion();
});

ipcMain.on('app:platform', (event) => {
  event.returnValue = process.platform;
});
