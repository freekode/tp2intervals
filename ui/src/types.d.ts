declare global {
  interface Window {
    electron: {
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
}

export {};
