import react from '@vitejs/plugin-react'
import eslint from 'vite-plugin-eslint'
import stylelint from 'vite-plugin-stylelint'
import {fileURLToPath} from "url";
import { viteMockServe } from 'vite-plugin-mock'

// https://vitejs.dev/config/
const buildConfig = {
  base: '/pensjon/selvbetjening/inntektsplanleggeren',
  build: {
    outDir: './dist'
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
  base: '/pensjon/selvbetjening/inntektsplanleggeren',
  build: {
    manifest: true,
    rollupOptions: {
      input: {
        app: './index.html',
      }
    }
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
      '/pensjon/selvbetjening/inntektsplanleggeren/api': {
        target: 'https://pensjon-selvbetjening-inntektsplanleggeren-frontend-borger-q2.intern.dev.nav.no',
        changeOrigin: true,
      },
      '/pensjon/selvbetjening/inntektsplanleggeren/persistence': {
        target: 'https://pensjon-selvbetjening-inntektsplanleggeren-frontend-borger-q2.intern.dev.nav.no',
        changeOrigin: true,
      }
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
