import { contextBridge } from 'electron';
import { bootAddressSupplier } from "./renderer";
import { bootHealthySupplier } from "./renderer";

contextBridge.exposeInMainWorld('bootAddress', bootAddressSupplier())
contextBridge.exposeInMainWorld('bootHealthy', bootHealthySupplier());
