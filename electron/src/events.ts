import EventEmitter from 'events';
import TypedEmitter from 'typed-emitter/rxjs';
import { ProgressInfo, UpdateInfo } from "electron-updater";

export const systemEvents = new EventEmitter() as TypedEmitter<Events>;

export type Events = {
  'boot-ready': () => void;
  'boot-healthy': () => void;
  'boot-unhealthy': () => void;

  /**
   * Updates
   */
  'checking-for-update': () => void;
  'update-available': (info: UpdateInfo) => void;
  'update-not-available': (info: UpdateInfo) => void;
  'update-download-progress': (info: ProgressInfo) => void;
  'update-downloaded': (info: UpdateInfo) => void;
  'update-error': (error: Error) => void;
  'update-cancelled': (info: UpdateInfo) => void;
};
