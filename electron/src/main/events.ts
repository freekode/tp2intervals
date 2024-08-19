import TypedEmitter from 'typed-emitter/rxjs';
import EventEmitter from 'events';

export const systemEvents = new EventEmitter() as TypedEmitter<Events>;

export type Events = {
  'boot-ready': () => void;
  'boot-healthy': () => void;
  'boot-unhealthy': () => void;
};
