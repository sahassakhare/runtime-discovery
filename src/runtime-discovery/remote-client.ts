import { loadRemoteModule as loadRemoteModuleFn, LoadRemoteModuleOptions } from '@angular-architects/module-federation';
import { loadRemoteWithSri } from './sri-loader';
import { RuntimeDiscovery, ResolveRemoteResponse } from './types';

/**
 * Interface for live monitoring services to avoid circular dependency
 */
export interface MonitoringService {
  monitor(remoteName: string): void;
}

/**
 * Client for interacting with remote modules.
 * Orchestrates resolution, integrity checking, and module loading.
 */
export type RemoteOptions = Partial<LoadRemoteModuleOptions> & {
  retries?: number;
  context?: Record<string, any>;
  type: 'module' | 'script' | 'manifest';
};

// Global Strategy Holder
let globalDiscovery: RuntimeDiscovery | null = null;
let globalMonitoring: MonitoringService | undefined = undefined;
let globalConfig: { appName: string; environment: string; apiUrl: string } | null = null;

export function setDiscoveryStrategy(
  discovery: RuntimeDiscovery,
  config: { appName: string; environment: string; apiUrl: string },
  monitoring?: MonitoringService
) {
  globalDiscovery = discovery;
  globalConfig = config;
  globalMonitoring = monitoring;
}

export async function loadRemoteModule<T = any>(
  remoteName: string,
  exposedModule: string,
  options: RemoteOptions
): Promise<T> {
  if (!globalDiscovery) {
    throw new Error('RuntimeDiscovery strategy not set. Call setDiscoveryStrategy() first.');
  }

  const { retries = 1, context, type } = options;

  console.debug(`[Maverick] Starting resolution for ${remoteName}...`);
  const resolved: ResolveRemoteResponse =
    await globalDiscovery.resolveRemote(remoteName, context);

  // Monitor if service available
  if (globalMonitoring) {
    globalMonitoring.monitor(remoteName);
  }

  // Telemetry: Record variant
  if (resolved.resolutionContext?.variant) {
    console.info(`[Maverick] Loaded variant '${resolved.resolutionContext.variant.name}' (${resolved.resolutionContext.variant.type}) for ${remoteName}`);
    if (typeof window !== 'undefined') {
      window.dispatchEvent(new CustomEvent('maverick:variant_loaded', { detail: resolved }));
    }
  }

  // Governance: Policy check
  if (resolved.resolutionContext?.governanceReason) {
    console.warn(`[Maverick] [GOVERNANCE] Action taken for ${remoteName}: ${resolved.resolutionContext.governanceReason}`);
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

  // Governance: Flags
  if (resolved.resolutionContext?.flags && typeof window !== 'undefined') {
    const globalFlags = (window as any).__MAVERICK_FLAGS__ || {};
    (window as any).__MAVERICK_FLAGS__ = { ...globalFlags, ...resolved.resolutionContext.flags };
    window.dispatchEvent(new CustomEvent('maverick:flags_updated', { detail: (window as any).__MAVERICK_FLAGS__ }));
  }

  // Helper to load variant
  const loadVariant = async (versionInfo: { remoteEntry: string, integrity?: string, type?: 'module' | 'script' | 'manifest' }) => {
    const remoteType = type || versionInfo.type || 'module';
    if (versionInfo.integrity) {
      console.log('[RemoteClient] Integrity found:', versionInfo.integrity);
      await loadRemoteWithSri(versionInfo.remoteEntry, versionInfo.integrity, remoteType);
    }

    // Dynamic Telemetry Reporting
    if (globalConfig) {
      fetch(`${globalConfig.apiUrl}/dashboard/telemetry/consumption`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          consumer: globalConfig.appName,
          remote: remoteName,
          modules: exposedModule,
          environment: globalConfig.environment,
          dynamic: true
        })
      }).catch(e => console.warn('[Maverick] Telemetry failed', e));
    }

    // Filter out custom options to avoid type errors with the library function
    const { retries: _r, context: _c, ...cleanOptions } = options;

    const loadOptions = {
      ...cleanOptions,
      type: remoteType,
      remoteEntry: versionInfo.remoteEntry,
      exposedModule: exposedModule
    } as LoadRemoteModuleOptions;

    console.debug('[Maverick] Calling loadRemoteModuleFn with:', JSON.stringify(loadOptions));

    return await loadRemoteModuleFn(loadOptions);
  };

  try {
    return await loadVariant(resolved.selected);
  } catch (err) {
    if (!resolved.fallback) throw err;
    console.warn(`[Maverick] Primary load failed. Fallback...`, err);
    return await loadVariant(resolved.fallback);
  }
}

export class RemoteClient {
  constructor(
    private discovery: RuntimeDiscovery,
    private monitoring?: MonitoringService
  ) { }

  /**
   * @deprecated Use standalone loadRemoteModule instead.
   */
  async loadRemoteModule<T>(
    remoteName: string,
    exposedModule: string,
    options: RemoteOptions
  ): Promise<T> {
    // Ensure strategy is set (backward compatibility)
    // We assume setDiscoveryStrategy was called, OR we can temporarily set it here if missing?
    // Safer to just proxy to the function.
    if (!globalDiscovery) {
      setDiscoveryStrategy(this.discovery, { appName: 'legacy', environment: 'production', apiUrl: '' }, this.monitoring);
    }
    return loadRemoteModule(remoteName, exposedModule, options);
  }
}
