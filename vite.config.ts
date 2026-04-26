import { defineConfig } from 'vite';
import { resolve } from 'path';

export default defineConfig(({ command }) => ({
  root: 'src/main/frontend',

  build: {
    outDir: '../resources/static/dist',
    emptyOutDir: true,
    manifest: false,
    rollupOptions: {
      input: {
        // Public/frontend entry points
        main: resolve(__dirname, 'src/main/frontend/css/front.css'),
        app: resolve(__dirname, 'src/main/frontend/js/main.ts'),
        // Admin entry points
        admin: resolve(__dirname, 'src/main/frontend/admin/css/admin.css'),
        'admin-app': resolve(__dirname, 'src/main/frontend/admin/js/admin.ts'),
        // Family tree entry points
        'family-tree': resolve(__dirname, 'src/main/frontend/js/apps/family-tree.ts'),
        'family-tree-css': resolve(__dirname, 'src/main/frontend/css/family-tree/base.css'),
      },
      output: {
        entryFileNames: 'assets/[name].js',
        chunkFileNames: 'assets/[name].js',
        assetFileNames: 'assets/[name].[ext]',
      },
    },
  },

  server: {
    port: 5173,
    strictPort: true,
    // Proxy API requests to Spring Boot during development
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },

  // Use '/' for dev server, '/dist/' for production build
  base: command === 'serve' ? '/' : '/dist/',
}));
