import { EnvironmentProviders, makeEnvironmentProviders, ENVIRONMENT_INITIALIZER } from '@angular/core';
import { HttpRuntimeDiscovery, registerApplication } from './runtime-discovery';
import { RemoteClient } from './remote-client';
import { DiscoveryConfig } from './types';
import { REMOTE_CLIENT, DISCOVERY_CONFIG } from './tokens';

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
                return new RemoteClient(discovery);
            }
        },
        {
            provide: ENVIRONMENT_INITIALIZER,
            multi: true,
            useValue: () => registerApplication(config)
        }
    ]);
}
