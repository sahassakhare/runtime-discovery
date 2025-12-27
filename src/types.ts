/**
 * Response type from the discovery service when resolving a remote module.
 */
export interface ResolveRemoteResponse {
  /**
   * The name of the remote.
   */
  remoteName: string;

  /**
   * The selected version details for the remote.
   */
  selected: {
    /**
     * Semantic version of the remote.
     */
    version: string;
    /**
     * URL to the remote entry file (e.g., remoteEntry.js).
     */
    remoteEntry: string;
    /**
     * Sub-Resource Integrity (SRI) hash for security validation.
     * Optional but recommended for production.
     */
    integrity?: string;
  };

  /**
   * Optional fallback version details if the selected version fails to load.
   */
  fallback?: {
    version: string;
    remoteEntry: string;
    integrity?: string;
  };

  /**
   * Recommended cache time-to-live in seconds for this resolution.
   */
  cacheTtlSeconds: number;
}

/**
 * Interface for a runtime discovery service that resolves remotes.
 */
export interface RuntimeDiscovery {
  /**
   * Resolves the remote entry URL and metadata for a given remote name.
   *
   * @param remoteName - The unique name of the remote to resolve.
   * @returns A promise that resolves to the remote module configuration.
   */
  resolveRemote(remoteName: string): Promise<ResolveRemoteResponse>;
}