import { contextBridge } from 'electron';
import { appVersionSupplier, bootAddressSupplier, bootHealthySupplier } from "./renderer";

contextBridge.exposeInMainWorld('bootAddress', bootAddressSupplier())
contextBridge.exposeInMainWorld('bootHealthy', bootHealthySupplier());
contextBridge.exposeInMainWorld('appVersion', appVersionSupplier());
