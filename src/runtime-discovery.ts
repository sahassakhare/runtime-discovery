/**
 * Key-Value implementation of the RuntimeDiscovery that communicates with a remote HTTP service.
 * It resolves remote locations dynamically based on the environment.
 */
export class HttpRuntimeDiscovery {
  /**
   * Creates an instance of HttpRuntimeDiscovery.
   *
   * @param discoveryUrl - Base URL of the discovery service.
   * @param environment - The current environment name (e.g., 'production', 'staging').
   */
  constructor(
    private discoveryUrl: string,
    private environment: string
  ) { }

  /**
   * Resolves the remote entry URL and metadata for a given remote name.
   * Sends a POST request to the discovery service with context.
   *
   * @param remoteName - The unique name of the remote to resolve.
   * @returns A promise that resolves to the remote module configuration.
   * @throws Will throw an error if the HTTP request fails.
   */
  async resolveRemote(remoteName: string) {
    const res = await fetch(`${this.discoveryUrl}/resolve`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        remoteName,
        environment: this.environment,
        context: {
          sessionId: crypto.randomUUID()
        }
      })
    });

    if (!res.ok) {
      const errorText = await res.text();
      throw new Error(`Discovery resolve failed: ${res.status} ${res.statusText} - ${errorText}`);
    }

    return res.json();
  }
}