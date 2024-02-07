import type { ForgeConfig } from '@electron-forge/shared-types';
import MakerSquirrel from '@electron-forge/maker-squirrel';
import MakerZIP from '@electron-forge/maker-zip';
import MakerDeb from '@electron-forge/maker-deb';
import VitePlugin from '@electron-forge/plugin-vite';

const config: ForgeConfig = {
  packagerConfig: {
    extraResource: [
      '../boot/build/libs/tp2intervals.jar',
      '../devops/jdktool/jdks/linux/x64',
      '../ui/dist/ui'
    ]
  },
  rebuildConfig: {},
  makers: [
    new MakerSquirrel({}),
    new MakerZIP({}, ['darwin']),
    new MakerDeb({})],
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
