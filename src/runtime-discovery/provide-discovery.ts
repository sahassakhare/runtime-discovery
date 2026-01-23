import { EnvironmentProviders, makeEnvironmentProviders, provideAppInitializer, inject } from '@angular/core';
import { runtimeDiscovery, registerApplication } from './runtime-discovery';
import { RemoteClient, setDiscoveryStrategy } from './remote-client';
import { DiscoveryConfig, RuntimeDiscovery } from './types';
import { REMOTE_CLIENT, DISCOVERY_CONFIG, RUNTIME_DISCOVERY } from './tokens';

/**
 * Initializes the Maverick Runtime Discovery system.
 * This provider must be added to your application's bootstrap configuration (e.g. `app.config.ts`).
 * 
 * It sets up:
 * 1. Global Identity (appName, environment)
 * 2. Self-Registration (registers this app with the dashboard)
 * 3. RemoteClient (centralized loader)
 * 
 * @example
 * ```typescript
 * providers: [
 *   provideDiscovery({
 *     url: 'http://discovery.acme.com',
 *     appName: 'host-shell',
 *     environment: 'production'
 *   })
 * ]
 * ```
 *
 * @param config - Configuration object containing Discovery URL and Identity
 * @returns An EnvironmentProviders object compatible with `bootstrapApplication`
 */
export function provideDiscovery(config: DiscoveryConfig): EnvironmentProviders {
    return makeEnvironmentProviders([
        {
            provide: DISCOVERY_CONFIG,
            useValue: config
        },
        {
            provide: RUNTIME_DISCOVERY,
            useFactory: () => runtimeDiscovery(config)
        },
        {
            provide: REMOTE_CLIENT,
            useFactory: (discovery: RuntimeDiscovery) => new RemoteClient(discovery),
            deps: [RUNTIME_DISCOVERY]
        },
        provideAppInitializer(() => {
            const discovery = inject(RUNTIME_DISCOVERY);
            setDiscoveryStrategy(discovery, {
                appName: config.appName,
                environment: config.environment,
                apiUrl: config.url
            });
            registerApplication(config);
        })
    ]);
}
