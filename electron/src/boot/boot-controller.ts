import { app, BrowserWindow } from 'electron';
import log from 'electron-log';
import { Process } from './process/process';
import { PackagedBootProcess } from './process/packaged-boot-process';
import { systemEvents } from "./events";

export class BootController {
  private started: boolean = false;

  private running: boolean = false;

  private healthCheckInterval: NodeJS.Timeout | undefined;

  constructor(private readonly process: Process) {
  }

  initializeSubscriptions(window: BrowserWindow) {
    systemEvents.on('boot-healthy', () => {
      window.webContents.send('app:bootHealthy');
    });

    systemEvents.on('boot-unhealthy', () => {
      window.webContents.send('app:bootUnhealthy');
    });
  }

  getProcessAddress(): string {
    return this.process.getAddress();
  }

  async start() {
    log.info('Starting boot process...');
    await this.process.start();
    this.startHealthCheck();
  }

  async stop() {
    log.info('Stopping daemon...');
    this.stopHealthCheck();
    await this.process.stop();
    this.started = false;
    this.running = false;
  }

  async restart() {
    await this.stop();
    await this.start();
  }

  isHealthy(): boolean {
    return this.started && this.running;
  }

  private startHealthCheck() {
    if (this.healthCheckInterval) {
      return;
    }
    log.info('Starting health check interval');
    this.healthCheckInterval = setInterval(async () => {
      log.silly('Running daemon health check...');
      const {healthy, message} = await this.process.doHealthCheck();
      log.silly(`Daemon health check result: ${healthy} - ${message}`);
      if (healthy) {
        if (!this.started) {
          log.info('Process is ready!');
          systemEvents.emit('boot-ready');
        } else if (!this.running && this.started) {
          log.info('Process is healthy!');
          systemEvents.emit('boot-healthy');
        }
        this.running = true;
        this.started = true;
      } else {
        if (this.running && this.started) {
          log.info('Process is unhealthy!');
          systemEvents.emit('boot-unhealthy');
        }
        this.running = false;
      }
    }, 1000);
  }

  private stopHealthCheck() {
    if (this.healthCheckInterval) {
      log.info('Stopping health check...');
      clearInterval(this.healthCheckInterval);
      this.healthCheckInterval = undefined;
    }
  }
}

let bootController: BootController | undefined;

export function getBootController(): BootController | undefined {
  return bootController;
}

export async function initBootController(): Promise<BootController> {
  if (bootController) {
    return bootController;
  }

  bootController = new BootController(new PackagedBootProcess());
  bootController.start().catch((e) => {
    log.error('Error starting boot', e);
    app.exit(1);
  });
  return bootController;
}
