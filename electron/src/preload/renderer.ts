import { ipcRenderer } from 'electron';

export const bootAddressSupplier = (): string => ipcRenderer.sendSync('boot:address');
export const bootHealthySupplier = (): boolean => ipcRenderer.sendSync('boot:healthy');
export const appVersionSupplier = (): string => ipcRenderer.sendSync('app:version');
