const isMac = process.platform === 'darwin';
const isWindows = process.platform === 'win32';
const isLinux = process.platform === 'linux';
const isDev = process.env.NODE_ENV === 'development'

export { isMac, isWindows, isLinux, isDev };
