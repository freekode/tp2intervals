declare global {
  interface Window {
    electron: {
      bootHealthy: () => boolean;
      appPlatform: string;
    }
  }
}

export {};
