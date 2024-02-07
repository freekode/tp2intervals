export type Events = {
  'boot-ready': () => void;
  'boot-healthy': () => void;
  'boot-unhealthy': () => void;

  // /**
  //  * Updates
  //  */
  // 'checking-for-update': () => void;
  // 'update-available': (info: UpdateInfo) => void;
  // 'update-not-available': (info: UpdateInfo) => void;
  // 'update-download-progress': (info: ProgressInfo) => void;
  // 'update-downloaded': (info: UpdateInfo) => void;
  // 'update-error': (error: Error) => void;
  // 'update-cancelled': (info: UpdateInfo) => void;
};
