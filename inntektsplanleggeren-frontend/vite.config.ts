import react from '@vitejs/plugin-react'
import { resolve } from 'path'
import { fileURLToPath } from 'url'
import { type ConfigEnv, loadEnv } from 'vite'
import { viteMockServe } from 'vite-plugin-mock'

// https://vitejs.dev/config/
const buildConfig = {
    base: '/uforetrygd/selvbetjening/inntektsplanleggeren',
    build: {
        outDir: './dist',
        rollupOptions: {
            input: {
                appBorger: resolve(__dirname, './index-borger.html'),
                appVeileder: resolve(__dirname, './index-veileder.html'),
            },
            external: ['./nais.js'],
        },
    },
    plugins: [react()],
    resolve: {
        alias: {
            '@': fileURLToPath(new URL('./src', import.meta.url)),
        },
    },
}

const devConfig = (env: Record<string, string>) => ({
    base: '/uforetrygd/selvbetjening/inntektsplanleggeren',
    build: {
        manifest: true,
        rollupOptions: {
            input: {
                appBorger: resolve(__dirname, './index-borger.html'),
                appVeileder: resolve(__dirname, './index-veileder.html'),
            },
            external: ['./nais.js'],
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
            '@': fileURLToPath(new URL('./src', import.meta.url)),
        },
    },
    server: {
        proxy: {
            '/uforetrygd/selvbetjening/inntektsplanleggeren/api': `${env.VITE_PROXY_BACKEND}`,
            '/uforetrygd/selvbetjening/inntektsplanleggeren/toggles': `${env.VITE_PROXY_BACKEND}`,
        },
    },
})

// https://vitejs.dev/config/
export default ({ command, mode }: ConfigEnv) => {
    const env = loadEnv(mode, process.cwd())
    if (command === 'serve') {
        return devConfig(env)
    } else {
        return buildConfig
    }
}
