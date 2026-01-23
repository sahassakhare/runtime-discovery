import { DiscoveryConfig } from './types';

/**
 * Key-Value implementation of the RuntimeDiscovery that communicates with a remote HTTP service.
 * It resolves remote locations dynamically based on the environment.
 */

/**
 * Registers the application instance with the discovery service.
 * This is a fire-and-forget operation intended to run on application startup.
 */
export function registerApplication(config: DiscoveryConfig) {
  if (typeof window === 'undefined') {
    return;
  }

  const payload = {
    appName: config.appName,
    environment: config.environment,
    url: window.location.origin
  };

  fetch(`${config.url}/registry/instances`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload)
  }).catch(err => console.warn('[Maverick] Failed to register instance:', err));
}

// Functional implementation of RuntimeDiscovery
export function runtimeDiscovery(config: DiscoveryConfig) {
  return {
    resolveRemote: async (remoteName: string, context?: Record<string, any>) => {
      const body: any = {
        remoteName,
        environment: config.environment,
        context: context || {}
      };

      if (config.tenantId) {
        body.tenantId = config.tenantId;
      }

      const res = await fetch(`${config.url}/resolve`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body)
      });

      if (!res.ok) {
        const errorText = await res.text();
        throw new Error(`Discovery resolve failed: ${res.status} ${res.statusText} - ${errorText}`);
      }

      return res.json();
    }
  };
}