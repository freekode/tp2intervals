import type { ForgeConfig } from '@electron-forge/shared-types';
import MakerSquirrel from '@electron-forge/maker-squirrel';
import MakerZIP from '@electron-forge/maker-zip';
import MakerDeb from '@electron-forge/maker-deb';
import MakerDMG from '@electron-forge/maker-dmg';
import AutoUnpackNativesPlugin from '@electron-forge/plugin-auto-unpack-natives';

const config: ForgeConfig = {
  packagerConfig: {
    asar: true,
    extraResource: [
      '../boot/build/libs/tp2intervals-static.jar',
      '../devops/jdktool/jdks/linux/x64'
    ],

  },
  rebuildConfig: {},
  makers: [
    new MakerSquirrel({}),
    new MakerZIP({}),
    new MakerDMG({}, ['darwin']),
    new MakerDeb({}, ['linux']),
  ],
  publishers: [],
  plugins: [new AutoUnpackNativesPlugin({})],
};

export default config;
