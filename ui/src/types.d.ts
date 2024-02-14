declare global {
  interface Window {
    bootAddress: string;
    bootHealthy: () => boolean;
    appVersion: string;
  }
}

export {};
