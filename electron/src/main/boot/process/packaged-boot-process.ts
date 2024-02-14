import { ActuatorProcess } from './actuator-process';
import path from 'path';
import { app } from 'electron';
import { ChildProcessWithoutNullStreams, spawn } from 'child_process';
import log from 'electron-log';
import { isWindows } from "../environment";

export class PackagedBootProcess extends ActuatorProcess {
  private readonly bootJarPath = path.join(process.resourcesPath, 'tp2intervals.jar');
  private readonly jdkPath = path.join(process.resourcesPath, 'jdk', 'bin', isWindows ? 'java.exe' : 'java');
  private readonly bootDbPath = path.join(app.getPath('userData'), 'tp2intervals.sqlite');
  private readonly port: number;
  private readonly address: string;

  private childProcess?: ChildProcessWithoutNullStreams = undefined;

  constructor() {
    super();
    this.port = Math.floor(Math.random() * 10000) + 10000;
    this.address = `http://localhost:${this.port}`;
  }

  getAddress(): string {
    return this.address;
  }

  async start(): Promise<void> {
    if (this.childProcess) {
      log.warn('Packaged Process is already started');
      return;
    }

    log.info('boot db location', this.bootDbPath);

    const env = {
      SERVER_PORT: String(this.port),
      SPRING_DATASOURCE_URL: `jdbc:sqlite:${this.bootDbPath}`,
      SPRING_PROFILES_ACTIVE: `prod`,
    };

    log.info('Running boot from jar...');
    this.childProcess = spawn(this.jdkPath, ['-jar', this.bootJarPath], {
      env: {...process.env, ...env},
    });
    this.initProcessEvents();
  }

  async stop(): Promise<void> {
    if (this.childProcess) {
      log.info('Stopping boot process...');
      this.childProcess.kill();
      this.childProcess = undefined;
    }
  }

  private initProcessEvents() {
    if (!this.childProcess) {
      return;
    }

    this.childProcess.stdout.on('data', (data) => {
      log.info(`boot: ${data}`);
    });

    this.childProcess.stderr.on('data', (data) => {
      log.error(`boot: ${data}`);
    });

    process.on('exit', () => {
      this.childProcess?.kill();
    });
  }
}
