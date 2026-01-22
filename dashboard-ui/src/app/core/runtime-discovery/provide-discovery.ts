
import { EnvironmentProviders, makeEnvironmentProviders, provideAppInitializer } from '@angular/core';
import { HttpRuntimeDiscovery, registerApplication } from './runtime-discovery';
import { REMOTE_CLIENT, DISCOVERY_CONFIG } from './tokens';
import { RemoteClient, setDiscoveryStrategy } from './remote-client';
import { DiscoveryConfig } from './types';

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
            provide: REMOTE_CLIENT,

            useFactory: () => {
                const discovery = new HttpRuntimeDiscovery(config.url, config.environment, config.appName, config.tenantId);
                // Initialize global strategy for standalone usage
                setDiscoveryStrategy(
                    discovery,
                    { appName: config.appName, environment: config.environment, apiUrl: config.url }
                );
                return new RemoteClient(discovery);
            }
        },
        provideAppInitializer(() => registerApplication(config))
    ]);
}
