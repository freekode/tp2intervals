import { contextBridge } from 'electron';
import { bootAddressSupplier } from "./boot/renderer";
import { bootHealthySupplier } from "./boot/renderer";

// contextBridge.exposeInMainWorld('bootAddress', bootAddressSupplier())
// contextBridge.exposeInMainWorld('bootHealthy', bootHealthySupplier());
