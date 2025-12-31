import { loadRemoteModule, LoadRemoteModuleOptions } from '@angular-architects/module-federation';
import { loadRemoteWithSri } from './sri-loader';
import { RuntimeDiscovery, ResolveRemoteResponse } from './types';

/**
 * Client for interacting with remote modules.
 * Orchestrates resolution, integrity checking, and module loading.
 */
export type RemoteOptions = Partial<LoadRemoteModuleOptions> & {
  retries?: number;
  context?: Record<string, any>;
  type: 'module' | 'script' | 'manifest';
};

export class RemoteClient {

  /**
   * Creates an instance of RemoteClient.
   *
   * @param discovery - The RuntimeDiscovery implementation to resolve remotes.
   */
  constructor(private discovery: RuntimeDiscovery) { }

  /**
   * Loads a specific exposed module from a remote.
   * Tries to load the primary version first, and optionally falls back to a secondary version on failure.
   *
   * @template T - The type of the loaded module.
   * @param remoteName - The name of the remote involved.
   * @param exposedModule - The path of the module exposed by the remote.
   * @param retries - Number of retry attempts using fallback versions (default is 1).
   * @param context - Optional context for canary/experiment targeting.
   * @returns A promise that resolves to the loaded module.
   * @throws Will throw an error if the remote cannot be loaded after all attempts.
   */
  async loadRemoteModule<T>(
    remoteName: string,
    exposedModule: string,
    options: RemoteOptions
  ): Promise<T> {
    const { retries = 1, context, type } = options;

    console.debug(`[Maverick] Starting resolution for ${remoteName}...`);
    const resolved: ResolveRemoteResponse =
      await this.discovery.resolveRemote(remoteName, context);

    // Telemetry: Record if a specific variant was loaded
    if (resolved.resolutionContext?.variant) {
      console.info(`[Maverick] Loaded variant '${resolved.resolutionContext.variant.name}' (${resolved.resolutionContext.variant.type}) for ${remoteName}`);
      // Dispatch event for analytics
      if (typeof window !== 'undefined') {
        window.dispatchEvent(new CustomEvent('maverick:variant_loaded', { detail: resolved }));
      }
    }
    console.debug(`[Maverick] Resolved ${remoteName} to ${resolved.selected.remoteEntry}`);

    // Governance: Check for Policy enforcement
    if (resolved.resolutionContext?.governanceReason) {
      console.warn(`[Maverick] [GOVERNANCE] Action taken for ${remoteName}: ${resolved.resolutionContext.governanceReason}`);

      // Dispatch governance event for UI (e.g., Toast notification)
      if (typeof window !== 'undefined') {
        window.dispatchEvent(new CustomEvent('maverick:governance_alert', {
          detail: {
            remoteName,
            reason: resolved.resolutionContext.governanceReason,
            timestamp: new Date().toISOString()
          }
        }));
      }
    }

    // Governance: Register Features/Flags if present
    if (resolved.resolutionContext?.flags && typeof window !== 'undefined') {
      const globalFlags = (window as any).__MAVERICK_FLAGS__ || {};
      (window as any).__MAVERICK_FLAGS__ = { ...globalFlags, ...resolved.resolutionContext.flags };
      // Dispatch update event
      window.dispatchEvent(new CustomEvent('maverick:flags_updated', { detail: (window as any).__MAVERICK_FLAGS__ }));
    }

    // Helper to load a single variant
    const loadVariant = async (versionInfo: { remoteEntry: string, integrity?: string, type?: 'module' | 'script' | 'manifest' }) => {
      // Priority: Options override -> Discovery response -> Default 'module'
      const remoteType = type || versionInfo.type || 'module';

      if (versionInfo.integrity) {
        console.log('[RemoteClient] Integrity found:', versionInfo.integrity);
        // Preload with SRI validation
        await loadRemoteWithSri(versionInfo.remoteEntry, versionInfo.integrity, remoteType);
      }
      return await loadRemoteModule<T>({
        ...options,
        type: remoteType,
        remoteEntry: versionInfo.remoteEntry,
        exposedModule: exposedModule
      } as any);
    };

    try {
      // 1. Try Primary
      return await loadVariant(resolved.selected);
    } catch (err) {
      if (!resolved.fallback) {
        throw err;
      }

      console.warn(`[Maverick] Primary load failed for ${remoteName}. Attempting fallback...`, err);

      // 2. Try Fallback
      try {
        return await loadVariant(resolved.fallback);
      } catch (fallbackErr) {
        console.error(`[Maverick] Fallback load also failed for ${remoteName}`, fallbackErr);
        throw fallbackErr;
      }
    }
  }
}
