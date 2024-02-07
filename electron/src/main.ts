import { app, BrowserWindow } from 'electron';
import path from 'path';
import './boot/main'
import { getBootController, initBootController } from "./boot/boot-controller";

if (require('electron-squirrel-startup')) {
  app.quit();
}

const createMainWindow = () => {
  const mainWindow = new BrowserWindow({
    width: 1280,
    height: 600,
    webPreferences: {
      preload: path.join(__dirname, 'preload.js'),
    },
  });

  if (app.isPackaged) {
    mainWindow.loadFile(path.join(__dirname, `../../../ui/browser/index.html`));
  } else {
    mainWindow.loadURL('http://localhost:4200');
  }

  getBootController()?.initializeSubscriptions(mainWindow);

  mainWindow.webContents.openDevTools();
};

app.whenReady()
  .then(async () => {
    createMainWindow();
    await initBootController()
  })
  .catch(console.log);

app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') {
    app.quit();
  }
});

app.on('activate', () => {
  if (BrowserWindow.getAllWindows().length === 0) {
    createMainWindow();
  }
});
