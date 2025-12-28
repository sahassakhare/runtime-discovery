import { EnvironmentProviders, makeEnvironmentProviders, InjectionToken, ENVIRONMENT_INITIALIZER } from '@angular/core';
import { HttpRuntimeDiscovery, registerApplication } from './runtime-discovery';
import { RemoteClient } from './remote-client';

/**
 * Injection token for the RemoteClient instance.
 */
export const REMOTE_CLIENT = new InjectionToken<RemoteClient>('REMOTE_CLIENT');

/**
 * Configures the Discovery Client properties.
 */
export interface DiscoveryConfig {
    /** Base URL of the discovery service */
    url: string;
    /** Environment name (e.g. 'production', 'staging') */
    environment: string;
    /** Application Name (e.g. 'shell-ui', 'payment-remote') */
    appName: string;
}

/**
 * Provides the RemoteClient and its dependencies.
 *
 * @param config - The configuration for the discovery service.
 * @returns An EnvironmentProviders object.
 */
export function provideDiscovery(config: DiscoveryConfig): EnvironmentProviders {
    return makeEnvironmentProviders([
        {
            provide: REMOTE_CLIENT,
            useFactory: () => {
                const discovery = new HttpRuntimeDiscovery(config.url, config.environment, config.appName);
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
