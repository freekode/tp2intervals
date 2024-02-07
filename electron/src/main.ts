import { app, BrowserWindow } from 'electron';
import path from 'path';
import { initBootProcess } from "./boot/bootProcess";

// Handle creating/removing shortcuts on Windows when installing/uninstalling.
if (require('electron-squirrel-startup')) {
  app.quit();
}

const createWindow = () => {
  // Create the browser window.
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

  mainWindow.webContents.openDevTools();
};

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
