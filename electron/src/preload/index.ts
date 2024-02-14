import { contextBridge } from 'electron';
import { bootAddressSupplier, bootHealthySupplier } from "./renderer";

contextBridge.exposeInMainWorld('bootAddress', bootAddressSupplier())
contextBridge.exposeInMainWorld('bootHealthy', bootHealthySupplier());
