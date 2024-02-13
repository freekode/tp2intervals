import { app, BrowserWindow, shell } from 'electron';
import path from 'path';
import './boot'
import { getBootController, initBootController } from './boot/boot-controller';
import { systemEvents } from './events';
import log from 'electron-log';
import { appUpdater, initializeAppUpdaterSubscriptions } from './autoupdate/appUpdater';


/*
todo
create test repo
make release
upload appimage, latest-linux.yml
update app-update.yml
 */

if (require('electron-squirrel-startup')) {
  app.quit();
}

appUpdater.checkForUpdates()

let splashWindow: BrowserWindow | null = null;
let mainWindow: BrowserWindow | null = null;

const getSplashWindowPageUrl = () => {
  if (app.isPackaged) {
    return 'file://' + path.join(__dirname, `../../../ui/browser/assets/loading.html`);
  } else {
    return 'http://localhost:4200/assets/loading.html';
  }
}

const getMainWindowPageUrl = () => {
  if (app.isPackaged) {
    return 'file://' + path.join(__dirname, `../../../ui/browser/index.html`);
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
    resizable: false,
    frame: false,
    center: true,
    webPreferences: {
      devTools: false,
      preload: path.join(__dirname, 'preload.js'),
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
    height: 700,
    minWidth: 500,
    minHeight: 400,
    // frame: isMac,
    // titleBarStyle: isMac ? 'hidden' : undefined,
    trafficLightPosition: {x: 12, y: 12},
    autoHideMenuBar: true,
    webPreferences: {
      preload: path.join(__dirname, 'preload.js'),
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

  // log.transports.console.level = 'info';
  initializeAppUpdaterSubscriptions(mainWindow);
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
