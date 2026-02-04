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
  type?: 'module' | 'script' | 'manifest';
  circuitBreaker?: Partial<CircuitBreakerConfig>;
};

/**
 * Circuit Breaker State
 */
enum CircuitState {
  CLOSED,
  OPEN,
  HALF_OPEN
}

/**
 * Circuit Breaker Configuration (Resilience4j style)
 */
export interface CircuitBreakerConfig {
  /** Explicitly enable or disable the circuit breaker */
  enabled: boolean;
  /** Number of failures before opening the circuit */
  failureThreshold: number;
  /** Time in milliseconds to wait before switching to half-open */
  waitDurationInOpenState: number;
}

const DEFAULT_CB_CONFIG: CircuitBreakerConfig = {
  enabled: true,
  failureThreshold: 3,
  waitDurationInOpenState: 10000
};

class CircuitBreaker {
  private failures = 0;
  private state = CircuitState.CLOSED;
  private nextAttempt = 0;

  constructor(private config: CircuitBreakerConfig = DEFAULT_CB_CONFIG) { }

  public updateConfig(newConfig: Partial<CircuitBreakerConfig>) {
    this.config = { ...this.config, ...newConfig };
  }

  public recordFailure(): void {
    if (!this.config.enabled) return;

    this.failures++;
    if (this.failures >= this.config.failureThreshold) {
      this.state = CircuitState.OPEN;
      this.nextAttempt = Date.now() + this.config.waitDurationInOpenState;
      console.warn(`[Maverick] Circuit Breaker OPEN for remote.`);
    }
  }

  public recordSuccess(): void {
    if (!this.config.enabled) return;
    this.failures = 0;
    this.state = CircuitState.CLOSED;
  }

  public canRequest(): boolean {
    if (!this.config.enabled) return true;

    if (this.state === CircuitState.CLOSED) return true;
    if (this.state === CircuitState.OPEN) {
      if (Date.now() > this.nextAttempt) {
        this.state = CircuitState.HALF_OPEN;
        return true;
      }
      return false;
    }
    return true; // HALF_OPEN
  }
}

const breakers = new Map<string, CircuitBreaker>();

function getBreaker(id: string, config?: Partial<CircuitBreakerConfig>): CircuitBreaker {
  if (!breakers.has(id)) {
    breakers.set(id, new CircuitBreaker(config ? { ...DEFAULT_CB_CONFIG, ...config } : DEFAULT_CB_CONFIG));
  }
  const breaker = breakers.get(id)!;
  if (config) {
    breaker.updateConfig(config);
  }
  return breaker;
}

// Global Strategy Holder
let globalDiscovery: RuntimeDiscovery | null = null;
let globalMonitoring: MonitoringService | undefined = undefined;
let globalConfig: { appName: string; environment: string; apiUrl: string } | null = null;

/**
 * Sets the global runtime strategy for the library.
 * This is internally called by `provideDiscovery()` during app initialization.
 * 
 * @param discovery - The strategy implementation (e.g. HTTP-based discovery)
 * @param config - Global identity configuration (appName, env, apiUrl)
 * @param monitoring - Optional monitoring service for uptime tracking
 */
export function setDiscoveryStrategy(
  discovery: RuntimeDiscovery,
  config: { appName: string; environment: string; apiUrl: string },
  monitoring?: MonitoringService
) {
  globalDiscovery = discovery;
  globalConfig = config;
  globalMonitoring = monitoring;
}

/**
 * Loads a remote module/component with full enterprise capabilities:
 * - **Discovery**: Resolves the best version based on Tenant/Env/Context.
 * - **Security**: Checks Integrity (SRI) and Governance policies.
 * - **Telemetry**: Reports consumption to the dashboard.
 * - **Resilience**: Automatically retries or falls back to stable versions on failure.
 * 
 * @example
 * ```typescript
 * const m = await loadRemoteModule('profile', './Profile', { 
 *   type: 'module',
 *   context: { 'user.role': 'beta' }
 * });
 * ```
 * 
 * @param remoteName - The registered name of the remote app (e.g. 'profile')
 * @param exposedModule - The exposed key in the remote's generic configuration (e.g. './Profile')
 * @param options - Configuration for loading (context, type, retries)
 * @returns A Promise resolving to the loaded module exports
 * @throws Error if the remote cannot be resolved or loaded after fallbacks
 */
   * @throws Error if the remote cannot be resolved or loaded after fallbacks
  */
export async function loadRemoteModule<T = any>(
  remoteName: string,
  exposedModule: string,
  options: RemoteOptions
): Promise<T> {
  return loadRemoteModuleInternal<T>(remoteName, exposedModule, options, false);
}

/**
 * Preloads a remote module to warm the cache.
 * Does not execute side effects or mount the component.
 */
export async function preloadRemoteModule(
  remoteName: string,
  exposedModule: string,
  options: RemoteOptions
): Promise<void> {
  try {
    console.debug(`[Maverick] Preloading ${remoteName}...`);
    await loadRemoteModuleInternal(remoteName, exposedModule, options, true);
  } catch (e) {
    console.warn(`[Maverick] Prefetch failed for ${remoteName}`, e);
  }
}

/**
 * Internal implementation of the remote loading logic.
 * Handles the full pipeline:
 * 1. Circuit Breaker check
 * 2. URL Override check (Hybrid Dev)
 * 3. Discovery Service resolution
 * 4. Policy & Governance enforcement
 * 5. Telemetry recording
 * 6. Variant loading (with SRI support)
 * 7. Preloading logic (if isPreload is true)
 * 
 * @internal
 */
async function loadRemoteModuleInternal<T = any>(
  remoteName: string,
  exposedModule: string,
  options: RemoteOptions,
  isPreload: boolean
): Promise<T> {
  if (!globalDiscovery) {
    throw new Error('RuntimeDiscovery strategy not set. Call setDiscoveryStrategy() first.');
  }

  const { retries = 1, context, type, circuitBreaker } = options;

  console.debug(`[Maverick] Starting resolution for ${remoteName}...`);

  const breaker = getBreaker(remoteName, circuitBreaker);
  if (!breaker.canRequest()) {
    throw new Error(`[CircuitBreaker] Circuit is OPEN for ${remoteName}. Fail fast.`);
  }

  // Hybrid Dev: Check for Overrides in URL
  // Pattern: ?override_profile=http://localhost:4201/remoteEntry.js
  let finalContext = { ...context };
  if (typeof window !== 'undefined') {
    const params = new URLSearchParams(window.location.search);
    const overrideKey = `override_${remoteName}`;
    if (params.has(overrideKey)) {
      const overrideUrl = params.get(overrideKey);
      console.warn(`[Maverick] Found Local Override for ${remoteName}: ${overrideUrl}`);
      // Send as "remoteName=url" to the single "mfe_override" key to keep it clean
      // If existing overrides exist, strictly we should merge, but for now simple 1-to-1 is fine.
      // Format expected by backend: "remoteName=url"
      finalContext['mfe_override'] = `${remoteName}=${overrideUrl}`;
    }
  }

  let resolved: ResolveRemoteResponse;
  try {
    resolved = await globalDiscovery.resolveRemote(remoteName, finalContext);
  } catch (err) {
    breaker.recordFailure();
    throw err;
  }

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

    if (globalConfig) {
      fetch(`${globalConfig.apiUrl}/registry/consumption`, {
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

    if (isPreload && (remoteType === 'script' || remoteType === 'module')) {
      // Just fetch the file to warm browser cache
      if (typeof window !== 'undefined' && (versionInfo.remoteEntry.startsWith('http') || versionInfo.remoteEntry.startsWith('/'))) {
        try {
          // Low-priority fetch for preloading
          const link = document.createElement('link');
          link.rel = 'prefetch'; // or 'modulepreload'
          link.href = versionInfo.remoteEntry;
          link.as = remoteType === 'script' ? 'script' : 'script';
          document.head.appendChild(link);
          return {} as T;
        } catch (e) {
          // Fallback to fetch if link not supported or just to be safe
          fetch(versionInfo.remoteEntry, { mode: 'cors', priority: 'low' }).catch(() => { });
          return {} as T;
        }
      }
      return {} as T;
    }

    return await loadRemoteModuleFn(loadOptions);
  };

  try {
    const result = await loadVariant(resolved.selected);
    breaker.recordSuccess();
    return result;
  } catch (err) {
    console.error(`[Maverick] Remote load failed for ${remoteName}`, err);
    // If we have a fallback, try it before tripping the breaker? 
    // Usually fallback is better than breaking, but if fallback also fails...

    if (resolved.fallback) {
      try {
        console.warn(`[Maverick] Attempting fallback for ${remoteName}...`);
        const fallbackResult = await loadVariant(resolved.fallback);
        // If fallback succeeds, do we strictly record success? 
        // Maybe not, the primary is technically down. But the USER functionality works.
        // Let's count it as partial success (don't trip breaker harder, but don't reset failures?).
        // For simplicity: success.
        breaker.recordSuccess();
        return fallbackResult;
      } catch (fallbackErr) {
        breaker.recordFailure();
        throw fallbackErr;
      }
    }

    breaker.recordFailure();
    throw err;
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

  async preload(remoteName: string, exposedModule: string, options: RemoteOptions) {
    return preloadRemoteModule(remoteName, exposedModule, options);
  }
}
