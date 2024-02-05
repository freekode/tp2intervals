import { ipcMain } from 'electron';
import { getBootProcess } from "./bootProcess";

ipcMain.on('boot:address', (event) => {
  event.returnValue = getBootProcess()?.address;
});
