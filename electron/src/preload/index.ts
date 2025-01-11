import { contextBridge } from 'electron';
import {
  bootHealthySupplier,
  platformSupplier,
} from "./renderer";

contextBridge.exposeInMainWorld('electron', {
  bootHealthy: bootHealthySupplier(),
  appPlatform: platformSupplier(),
})
