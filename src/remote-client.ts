import { loadRemoteModule } from '@angular-architects/module-federation';
import { loadRemoteWithSri } from './sri-loader';
import { RuntimeDiscovery, ResolveRemoteResponse } from './types';

/**
 * Client for interacting with remote modules.
 * Orchestrates resolution, integrity checking, and module loading.
 */
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
   * @returns A promise that resolves to the loaded module.
   * @throws Will throw an error if the remote cannot be loaded after all attempts.
   */
  async loadRemoteModule<T>(
    remoteName: string,
    exposedModule: string,
    retries = 1
  ): Promise<T> {

    const resolved: ResolveRemoteResponse =
      await this.discovery.resolveRemote(remoteName);

    try {
      await loadRemoteWithSri(
        resolved.selected.remoteEntry,
        resolved.selected.integrity
      );

      return await loadRemoteModule({
        remoteName: remoteName,
        exposedModule
      }) as T;

    } catch (err) {
      if (retries > 0 && resolved.fallback) {
        console.warn(`[RemoteClient] retrying with fallback version`, resolved.fallback);
        await loadRemoteWithSri(
          resolved.fallback.remoteEntry,
          resolved.fallback.integrity
        );

        return await loadRemoteModule({
          remoteName: remoteName,
          exposedModule
        }) as T;
      }
      throw err;
    }
  }
}