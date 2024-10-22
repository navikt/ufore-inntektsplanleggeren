import react from "@vitejs/plugin-react";
import eslint from "vite-plugin-eslint";
import stylelint from "vite-plugin-stylelint";
import { fileURLToPath } from "url";
import { resolve } from "path";

// https://vitejs.dev/config/
const buildConfig = () => ({
  base: "/pensjon/selvbetjening/inntektsplanleggeren",
  build: {
    outDir: "./dist",
    target: "esnext",
    // rollupOptions: {
    //   input: {
    //     appBorger: resolve(__dirname, "./index.html"),
    //     // appVeileder: resolve(__dirname, "./index-veileder.html"),
    //   },
    // },
  },
  plugins: [react(), eslint(), stylelint({ fix: true })],
  resolve: {
    alias: {
      "@": fileURLToPath(new URL("./src", import.meta.url)),
    },
  },
});

// https://vitejs.dev/config/
export default () => {
  return buildConfig();
};
