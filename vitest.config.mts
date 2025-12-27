import { defineConfig } from 'vitest/config';

export default defineConfig({
    test: {
        globals: true,
        environment: 'happy-dom',
        setupFiles: [],
        exclude: ['node_modules', 'dist'],
        alias: {
            '@angular-architects/module-federation': '@angular-architects/module-federation-runtime'
        }
    },
});
