import { app, BrowserWindow, shell } from 'electron';
import path from 'path';
import './boot'
import { getBootController, initBootController } from './boot/boot-controller';
import { systemEvents } from './events';
import log from 'electron-log';


if (require('electron-squirrel-startup')) {
  app.quit();
}

let splashWindow: BrowserWindow | null = null;
let mainWindow: BrowserWindow | null = null;

const RESOURCES_PATH = app.isPackaged
  ? path.join(process.resourcesPath, 'assets')
  : path.join(__dirname, '../../assets');

const getAssetPath = (...paths: string[]): string => {
  return path.join(RESOURCES_PATH, ...paths);
};

const getSplashWindowPageUrl = () => {
  if (app.isPackaged) {
    return 'file://' + path.join(__dirname, `../../../browser/assets/loading.html`);
  } else {
    return 'http://localhost:4200/assets/loading.html';
  }
}

const getMainWindowPageUrl = () => {
  if (app.isPackaged) {
    return 'file://' + path.join(__dirname, `../../../browser/index.html`);
  } else {
    return 'http://localhost:4200';
  }
}

const createSplashWindow = async () => {
  // todo add icon
  splashWindow = new BrowserWindow({
    show: true,
    title: 'Loading',
    width: 500,
    height: 300,
    icon: getAssetPath('icon.png'),
    resizable: false,
    frame: false,
    center: true,
    webPreferences: {
      devTools: false,
      preload: path.join(__dirname, '../preload/index.js'),
    },
  });

  splashWindow.loadURL(getSplashWindowPageUrl())

  splashWindow.on('ready-to-show', () => {
    if (!splashWindow) {
      throw new Error('"splashWindow" is not defined');
    }
    splashWindow.show();
  });

  splashWindow.on('closed', () => {
    splashWindow = null;
  });
};

const createMainWindow = async () => {
  // todo add icon
  mainWindow = new BrowserWindow({
    show: false,
    width: 650,
    height: 750,
    minWidth: 500,
    minHeight: 450,
    icon: getAssetPath('icon.png'),
    trafficLightPosition: {x: 12, y: 12},
    autoHideMenuBar: true,
    webPreferences: {
      preload: path.join(__dirname, '../preload/index.js'),
    },
  });

  mainWindow.loadURL(getMainWindowPageUrl())

  mainWindow.on('ready-to-show', () => {
    if (!mainWindow) {
      throw new Error('"mainWindow" is not defined');
    }
    if (splashWindow) {
      splashWindow.close();
      splashWindow = null;
    }
    mainWindow.show();
    mainWindow.focus();
  });

  mainWindow.on('closed', () => {
    mainWindow = null;
  });

  // Open urls in the user's browser
  mainWindow.webContents.setWindowOpenHandler((edata) => {
    shell.openExternal(edata.url);
    return {action: 'deny'};
  });

  getBootController()?.initializeSubscriptions(mainWindow);

  log.transports.console.level = 'info';
};

app.whenReady()
  .then(async () => {
    await createSplashWindow();
    systemEvents.on('boot-ready', () => {
      log.info('Creating main window (boot ready event)');
      createMainWindow();
    });
    await initBootController()
  })
  .catch(console.log);

app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') {
    app.quit();
  }
});
