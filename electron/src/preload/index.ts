import { contextBridge } from 'electron';
import {
  appVersionSupplier,
  bootAddressSupplier,
  bootHealthySupplier,
  platformSupplier,
} from "./renderer";

contextBridge.exposeInMainWorld('electron', {
  bootAddress: bootAddressSupplier(),
  bootHealthy: bootHealthySupplier(),
  appVersion: appVersionSupplier(),
  appPlatform: platformSupplier(),
})
