import { contextBridge, ipcRenderer } from 'electron';
import { bootAddressSupplier } from "./boot/renderer";

// contextBridge.exposeInMainWorld('bootAddress', bootAddressSupplier())

contextBridge.exposeInMainWorld('electron', {
  send: (channel: string, data: any) => {
    ipcRenderer.send(channel, data);
  },
  on: (channel: string, func: (...args: any[]) => void) => {
    const newFunc = (...args: any[]) => func(...args);
    ipcRenderer.on(channel, newFunc);
  },
  sendSync: (channel: string, data: any) => {
    return ipcRenderer.sendSync(channel, data);
  },
  removeListener: (channel: string, func: (...args: any[]) => void) => {
    ipcRenderer.removeListener(channel, func);
  },
})
