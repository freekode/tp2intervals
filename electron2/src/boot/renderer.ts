import { ipcRenderer } from 'electron';

export const bootAddressSupplier = (): string => ipcRenderer.sendSync('boot:address');
