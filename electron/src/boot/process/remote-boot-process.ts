import log from 'electron-log';
import { ActuatorProcess } from "./actuator-process";

export class RemoteDaemon extends ActuatorProcess {
  private readonly address: string;

  constructor(address: string) {
    super();
    this.address = address;
  }

  getAddress(): string {
    return this.address;
  }

  async start(): Promise<void> {
    log.warn('Cant start emote process');
  }

  async stop(): Promise<void> {
    log.warn('Cant stop remote process');
  }
}
