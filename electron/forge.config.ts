import type { ForgeConfig } from '@electron-forge/shared-types';
import MakerSquirrel from '@electron-forge/maker-squirrel';
import MakerZIP from '@electron-forge/maker-zip';
import VitePlugin from '@electron-forge/plugin-vite';
import PublisherGithub from '@electron-forge/publisher-github';
import MakerAppImage from "electron-forge-maker-appimage";

const config: ForgeConfig = {
  packagerConfig: {
    extraResource: [
      '../boot/build/libs/tp2intervals.jar',
      '../devops/jdktool/jdks/${process.platform}/${process.arch}',
      '../ui/dist/ui',
      'src/autoupdate/app-dev-update.yml',
      'src/autoupdate/app-update.yml'
    ]
  },
  makers: [
    new MakerSquirrel({}),
    new MakerZIP({}, ['darwin']),
    new MakerAppImage()
  ],
  plugins: [
    new VitePlugin({
      build: [
        {
          entry: 'src/main.ts',
          config: 'vite.main.config.ts',
        },
        {
          entry: 'src/preload.ts',
          config: 'vite.preload.config.ts',
        },
      ],
      renderer: [],
    }),
  ],
};

export default config;
