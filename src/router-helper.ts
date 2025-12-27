import { Route } from '@angular/router';
import { RemoteClient } from './remote-client';

/**
 * Helper to create an Angular Route for a lazy-loaded remote.
 *
 * @param remoteClient - Instance of the RemoteClient service.
 * @param remoteName - Name of the remote to route to.
 * @param exposedModule - The module path exposed by the remote.
 * @returns An Angular Route object configured for lazy loading.
 */
export function remoteRoute(
  remoteClient: RemoteClient,
  remoteName: string,
  exposedModule: string
): Route {
  return {
    loadChildren: async () => {
      const mod = await remoteClient.loadRemoteModule<any>(remoteName, exposedModule);
      return mod;
    }
  };
}