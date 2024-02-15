import TypedEmitter from 'typed-emitter/rxjs';
import { ProgressInfo, UpdateDownloadedEvent, UpdateInfo } from "electron-updater";
import EventEmitter from 'events';

export const systemEvents = new EventEmitter() as TypedEmitter<Events>;

export type Events = {
  'boot-ready': () => void;
  'boot-healthy': () => void;
  'boot-unhealthy': () => void;

  /**
   * Updates
   */
  'update-available': (info: UpdateInfo) => void;
  'update-downloaded': (event: UpdateDownloadedEvent) => void;
  'update-error': (error: Error) => void;
};
