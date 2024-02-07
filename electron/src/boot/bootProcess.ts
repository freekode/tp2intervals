import { ChildProcessWithoutNullStreams, spawn } from 'child_process';
import path from 'path';
import { app } from 'electron';
import log from 'electron-log';
import { isWindows } from "./platform";

export class BootProcess {
  private readonly bootJarPath = path.join(process.resourcesPath, 'tp2intervals.jar');
  private readonly jdkPath = path.join(process.resourcesPath, 'x64', 'jdk', 'bin', isWindows ? 'java.exe' : 'java');
  private readonly bootDbPath = path.join(app.getPath('userData'), 'tp2intervals.sqlite');
  private readonly bootLogPath = app.getPath('logs');
  private readonly port: number;
  readonly address: string;

  private childProcess?: ChildProcessWithoutNullStreams = undefined;

  constructor() {
    this.port = Math.floor(Math.random() * 10000) + 10000;
    this.address = `http://localhost:${this.port}`;
  }

  async start(): Promise<void> {
    log.info('boot db location', this.bootDbPath);
    log.info('boot log location', this.bootLogPath);

    if (this.childProcess) {
      log.warn('Packaged boot is already started');
      return;
    }

    const env = {
      SERVER_PORT: String(this.port),
      SPRING_DATASOURCE_URL: `jdbc:sqlite:${this.bootDbPath}`,
      LOGGING_FILE_PATH: String(this.bootLogPath),
    };

    log.info('Running boot from jar...');
    this.childProcess = spawn(this.jdkPath, ['-jar', this.bootJarPath], {
      env: {...process.env, ...env},
    });
    this.initProcessEvents();
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

let bootProcess: BootProcess | undefined;

export function getBootProcess(): BootProcess | undefined {
  return bootProcess;
}

export async function initBootProcess(): Promise<BootProcess> {
  if (bootProcess) {
    return bootProcess;
  }
  bootProcess = new BootProcess();
  bootProcess.start().catch((e) => {
    log.error('Error starting daemon', e);
    app.exit(1);
  });
  return bootProcess;
}
