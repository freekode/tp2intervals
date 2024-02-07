declare global {
  interface Window {
    bootAddress: string;
    bootHealthy: () => boolean;
  }
}

export {};
