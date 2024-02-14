import { ipcMain } from 'electron';
import { getBootController } from './boot-controller';

ipcMain.on('boot:address', (event) => {
  event.returnValue = getBootController()?.getProcessAddress();
});

ipcMain.on('boot:healthy', (event) => {
  event.returnValue = getBootController()?.isHealthy() ?? false;
});
