import { ApplicationRef, Type } from '@angular/core';
import { createApplication } from '@angular/platform-browser';
import { ApplicationConfig } from '@angular/core';
import { RemoteLifecycle } from './lifecycle';

/**
 * Options for creating an Angular Remote.
 */
export interface AngularRemoteOptions {
    /**
     * The root component to bootstrap.
     */
    component: Type<any>;
    /**
     * The application configuration (providers, etc.).
     */
    config: ApplicationConfig;
    /**
     * Optional: Custom isReady logic.
     */
    isReady?: () => Promise<boolean>;
}

/**
 * Creates a standard RemoteLifecycle implementation for an Angular Standalone Component.
 * abstracts away the manual mount/unmount logic.
 * 
 * @example
 * export const { mount, unmount, isReady } = defineRemote({
 *   component: AppComponent,
 *   config: appConfig
 * });
 */
export function defineRemote(options: AngularRemoteOptions): RemoteLifecycle {
    let appRef: ApplicationRef | null = null;
    const { component, config, isReady } = options;

    return {
        isReady: async () => {
            if (isReady) {
                return await isReady();
            }
            return true;
        },

        mount: async (container: HTMLElement, props: Record<string, any>) => {
            try {
                // 1. Create the application
                appRef = await createApplication(config);

                // 2. Bootstrap the component into the container
                // Note: bootstrap() typically expects to find the selector in the DOM. 
                // However, we can use the ComponentFactory to attach to a specific element.
                // Or simpler: We rely on createApplication and then bootstrap via the ref.

                const componentRef = appRef.bootstrap(component, container);

                // 3. Pass inputs
                if (props && componentRef.instance) {
                    Object.assign(componentRef.instance, props);
                    componentRef.changeDetectorRef.detectChanges();
                }
            } catch (err) {
                console.error('[AngularRemote] Failed to mount:', err);
                throw err;
            }
        },

        unmount: async (container: HTMLElement) => {
            if (appRef) {
                appRef.destroy();
                appRef = null;
            }
            container.innerHTML = '';
        }
    };
}
