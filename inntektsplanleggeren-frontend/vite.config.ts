import react from '@vitejs/plugin-react'
import eslint from 'vite-plugin-eslint'
import stylelint from 'vite-plugin-stylelint'
import {fileURLToPath} from "url";
import { viteMockServe } from 'vite-plugin-mock'
import { resolve } from "path";

// https://vitejs.dev/config/
const buildConfig = {
  base: '/uforetrygd/selvbetjening/inntektsplanleggeren',
  build: {
    outDir: './dist',
    rollupOptions: {
      input: {
        appBorger: resolve(__dirname, "./index.html"),
        appVeileder: resolve(__dirname, "./index-veileder.html"),
      },
    },
  },
  plugins: [
    react(),
    eslint(),
    stylelint({ fix: true }),
  ],
  resolve: {
    alias: {
      "@": fileURLToPath(new URL("./src", import.meta.url))
    }
  },
}

const devConfig = {
  base: '/uforetrygd/selvbetjening/inntektsplanleggeren',
  build: {
    manifest: true,
    rollupOptions: {
      input: {
        appBorger: resolve(__dirname, "./index.html"),
        appVeileder: resolve(__dirname, "./index-veileder.html"),
      },
    },
  },
  plugins: [
    react(),
    viteMockServe({
      // default
      mockPath: 'mock',
      enable: true,
    }),
  ],
  resolve: {
    alias: {
      "@": fileURLToPath(new URL("./src", import.meta.url))
    }
  },
  server: {
    port: 3000,
    proxy: {
      '/uforetrygd/selvbetjening/inntektsplanleggeren/api': {
        target: 'https://pensjon-selvbetjening-inntektsplanleggeren-frontend-borger-q2.intern.dev.nav.no',
        changeOrigin: true,
      },
    }
  }
}

// https://vitejs.dev/config/
export default ({ command }) => {
  if(command == 'serve') {
    return devConfig
  } else {
    return buildConfig
  }
}
