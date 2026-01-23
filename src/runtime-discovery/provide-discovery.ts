import { EnvironmentProviders, makeEnvironmentProviders, provideAppInitializer, inject } from '@angular/core';
import { runtimeDiscovery, registerApplication } from './runtime-discovery';
import { RemoteClient, setDiscoveryStrategy } from './remote-client';
import { DiscoveryConfig, RuntimeDiscovery } from './types';
import { REMOTE_CLIENT, DISCOVERY_CONFIG, RUNTIME_DISCOVERY } from './tokens';

/**
 * Provides the RemoteClient and its dependencies.
 *
 * @param config - The configuration for the discovery service.
 * @returns An EnvironmentProviders object.
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
