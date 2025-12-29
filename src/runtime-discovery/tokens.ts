import { InjectionToken } from '@angular/core';
import { RemoteClient } from './remote-client';
import { DiscoveryConfig } from './types';

/**
 * Injection token for the RemoteClient instance.
 */
export const REMOTE_CLIENT = new InjectionToken<RemoteClient>('REMOTE_CLIENT');

/**
 * Injection token for the DiscoveryConfig.
 */
export const DISCOVERY_CONFIG = new InjectionToken<DiscoveryConfig>('DISCOVERY_CONFIG');
