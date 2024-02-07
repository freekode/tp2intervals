export type ProcessHealthResult = {
  healthy: boolean;
  message?: string;
};

export interface Process {
  doHealthCheck(): Promise<ProcessHealthResult>;

  start(): Promise<void>;

  stop(): Promise<void>;

  getAddress(): string;

}
