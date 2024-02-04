import { app, BrowserWindow } from 'electron';
import installExtension from 'electron-devtools-installer';
import path from 'path';

// Handle creating/removing shortcuts on Windows when installing/uninstalling.
if (require('electron-squirrel-startup')) {
  app.quit();
}

let mainWindow: BrowserWindow | null;

const createWindow = (): void => {
  // Create the browser window.
  mainWindow = new BrowserWindow({
    height: 800,
    width: 1280,
    webPreferences: {
      preload: path.join(__dirname, 'preload.js'),
    },
  });

  // This should be the name of your angular project instead of `my-app`
  const startURL = app.isPackaged
    ? `file://${path.join(__dirname, 'my-app', 'index.html')}`
    : `http://localhost:4200`;

  mainWindow.loadURL(startURL);
};

app.whenReady().then(() => {
  // Install Angular DevTools
  // https://angular.io/guide/devtools
  installExtension('ienfalfjdbdpebioblfackkekamfmbnh')
    .then((name) => console.log(`Added Extension:  ${name}`))
    .catch((err) => console.log('An error occurred: ', err));
});

// This method will be called when Electron has finished
// initialization and is ready to create browser windows.
// Some APIs can only be used after this event occurs.
app.on('ready', () => {
  createWindow();
});

// Quit when all windows are closed, except on macOS. There, it's common
// for applications and their menu bar to stay active until the user quits
// explicitly with Cmd + Q.
app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') {
    app.quit();
  }
});

app.on('activate', () => {
  // On OS X it's common to re-create a window in the app when the
  // dock icon is clicked and there are no other windows open.
  if (BrowserWindow.getAllWindows().length === 0) {
    createWindow();
  }
});

// In this file you can include the rest of your app's specific main process
// code. You can also put them in separate files and import them here.
