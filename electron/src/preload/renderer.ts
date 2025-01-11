import { ipcRenderer } from 'electron';

export const bootHealthySupplier = (): boolean => ipcRenderer.sendSync('boot:healthy');
export const platformSupplier = (): string => ipcRenderer.sendSync('app:platform');
