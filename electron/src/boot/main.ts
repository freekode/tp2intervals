import { ipcMain } from 'electron';
import { getBootProcess } from "./bootProcess";

ipcMain.on('boot:address', (event) => {
  console.log('A@@@@@@')
  event.returnValue = getBootProcess()?.address;
});
