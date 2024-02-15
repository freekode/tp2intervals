declare global {
  interface Window {
    bootAddress: string;
    bootHealthy: () => boolean;
    appVersion: string;
    appPlatform: string;
    subscriptions: {
      'appUpdateAvailable',
      'appUpdateDownloaded'
    };
  }
}

export {};
