import { app, BrowserWindow } from 'electron';
import installExtension from 'electron-devtools-installer';
import path from 'path';
import { initBootProcess } from "./boot/bootProcess";

if (require('electron-squirrel-startup')) {
  app.quit();
}

let mainWindow: BrowserWindow | null;

const createWindow = () => {
  mainWindow = new BrowserWindow({
    height: 800,
    width: 1280,
    webPreferences: {
      preload: path.join(__dirname, 'preload.js'),
    },
  });

  const startURL = app.isPackaged
    ? `file://${path.join(__dirname, 'browser', 'index.html')}`
    : `http://localhost:4200`;

  mainWindow.loadURL(startURL);
};

const installDevTools = () => {
  installExtension('ienfalfjdbdpebioblfackkekamfmbnh')
    .then((name) => console.log(`Added Extension:  ${name}`))
    .catch((err) => console.log('An error occurred: ', err));
}

app.whenReady()
  .then(async () => {
    createWindow();
    await initBootProcess()
  })
  .catch(console.log);

app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') {
    app.quit();
  }
});

app.on('activate', () => {
  if (BrowserWindow.getAllWindows().length === 0) {
    createWindow();
  }
});
