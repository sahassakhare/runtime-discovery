import { DiscoveryConfig } from './provide-discovery';

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
    url: window.location.href
  };

  fetch(`${config.url}/registry/instances`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload)
  }).catch(err => console.warn('[Maverick] Failed to register instance:', err));
}

export class HttpRuntimeDiscovery {
  /**
   * Creates an instance of HttpRuntimeDiscovery.
   *
   * @param discoveryUrl - Base URL of the discovery service.
   * @param environment - The current environment name (e.g., 'production', 'staging').
   * @param appName - The name of the current application.
   */
  constructor(
    private discoveryUrl: string,
    private environment: string,
    private appName: string
  ) { }

  /**
   * Resolves the remote entry URL and metadata for a given remote name.
   * Sends a POST request to the discovery service with context.
   *
   * @param remoteName - The unique name of the remote to resolve.
   * @param context - Optional context (user ID, roles, etc.) for targeting.
   * @returns A promise that resolves to the remote module configuration.
   * @throws Will throw an error if the HTTP request fails.
   */
  async resolveRemote(remoteName: string, context?: Record<string, any>) {
    const body = {
      remoteName,
      environment: this.environment,
      context: context || {}
    };

    const res = await fetch(`${this.discoveryUrl}/resolve`, {
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
}