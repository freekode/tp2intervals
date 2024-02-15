import { contextBridge, ipcRenderer } from 'electron';
import {
  appVersionSupplier,
  bootAddressSupplier,
  bootHealthySupplier,
  platformSupplier,
  subscriptions
} from "./renderer";

contextBridge.exposeInMainWorld('bootAddress', bootAddressSupplier())
contextBridge.exposeInMainWorld('bootHealthy', bootHealthySupplier());
contextBridge.exposeInMainWorld('appVersion', appVersionSupplier());
contextBridge.exposeInMainWorld('appPlatform', platformSupplier());
contextBridge.exposeInMainWorld('subscriptions', subscriptions);
