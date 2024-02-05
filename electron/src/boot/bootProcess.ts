import { ChildProcessWithoutNullStreams, spawn } from 'child_process';
import path from 'path';
import { app } from 'electron';
import log from 'electron-log';
import { isWindows } from "./platform";

export class BootProcess {
    private readonly bootPath = path.join(process.resourcesPath, 'tp2intervals.jar');
    private readonly bootDbPath = path.join(app.getPath('userData'), 'tp2intervals.sqlite');
    private readonly jdkPath = path.join(process.resourcesPath, 'x64', 'jdk', 'bin', isWindows ? 'java.exe' : 'java');
    private readonly port: number;
    private readonly address: string;

    private childProcess?: ChildProcessWithoutNullStreams = undefined;

    constructor() {
        this.port = Math.floor(Math.random() * 10000) + 10000;
        this.address = `http://localhost:${this.port}`;
    }

    async start(): Promise<void> {
        if (this.childProcess) {
            log.warn('Packaged boot is already started');
            return;
        }

        const env = {
            SERVER_PORT: String(this.port),
            SPRING_DATASOURCE_URL: `jdbc:sqlite:${this.bootDbPath}`,
        };

        log.info('Running boot from jar...');
        this.childProcess = spawn(this.jdkPath, ['-jar', this.bootPath], {
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
