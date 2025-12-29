/**
 * Runtime Discovery Types
 * -----------------------
 * Defines the core interfaces for the Client-Side Discovery Library.
 * These types map directly to the backend API Contract (/api/resolve).
 * 
 * Key Updates:
 * - `resolutionContext`: Groups metadata (flags, variants) for scalable governance.
 */
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
     * Optional but recommended for production.
     */
    integrity?: string;
    /**
     * The type of the remote (e.g., 'module', 'script', 'manifest').
     * Defaults to 'module' if not specified.
     */
    type?: 'module' | 'script' | 'manifest';
  };

  /**
   * Optional fallback version details if the selected version fails to load.
   */
  fallback?: {
    version: string;
    remoteEntry: string;
    integrity?: string;
    type?: 'module' | 'script' | 'manifest';
  };


  /**
   * Contextual information about the resolution, including flags and variant.
   */
  resolutionContext?: {
    /**
     * Optional variant information (e.g., for canary or A/B testing).
     */
    variant?: {
      name: string;
      type: 'canary' | 'experiment' | 'standard';
    };

    /**
     * Application-level feature flags scoped to this remote.
     * Can be simple booleans (evaluated) or configuration objects (policies).
     */
    flags?: Record<string, any>;
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
   * @param context - Optional context (user ID, roles, etc.) for targeting.
   * @returns A promise that resolves to the remote module configuration.
   */
  resolveRemote(remoteName: string, context?: Record<string, any>): Promise<ResolveRemoteResponse>;
}

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
  /** Optional Tenant ID for multi-tenant resolution */
  tenantId?: string;
}