import { defineRemote } from '../../../../src/runtime-discovery/remote-adapter';
import { appConfig } from './app.config';
import { AppComponent } from './app.component';

/**
 * Concrete implementation using the Helper Utility.
 * Reduces 50 lines of boilerplate to just 6 lines.
 */
export const { mount, unmount, isReady } = defineRemote({
    component: AppComponent,
    config: appConfig,
    isReady: async () => {
        console.log('[Remote-Profile] Checking readiness (via helper)...');
        return new Promise(resolve => setTimeout(() => resolve(true), 100));
    }
});
