import axios from 'axios';
import { Process, ProcessHealthResult } from './process';

export abstract class ActuatorProcess implements Process {
  async doHealthCheck(): Promise<ProcessHealthResult> {
    try {
      const response = await axios.get(`${this.getAddress()}/actuator/health`);
      if (response.status >= 200 && response.status < 300) {
        return { healthy: true };
      }

      return { healthy: false, message: `Received status code ${response.status}` };
    } catch (err) {
      if (err instanceof Error) {
        return { healthy: false, message: err.message };
      }

      return { healthy: false, message: `Unknown error: ${err}` };
    }
  }

  abstract getAddress(): string;

  abstract start(): Promise<void>;

  abstract stop(): Promise<void>;
}
