import { Routes } from '@angular/router';
import { AppInjector } from './app-injector';
import { REMOTE_CLIENT } from '../../../../src/runtime-discovery/tokens';
import { RemoteClient } from '../../../../src/runtime-discovery/remote-client';
import { loadRemoteModule } from '@angular-architects/module-federation';
import { AuthService } from './auth.service';
import { LiveProfileComponent } from './live-profile/live-profile.component';

export const routes: Routes = [
    {
        path: '',
        pathMatch: 'full',
        redirectTo: 'profile'
    },
    {
        path: 'profile',
        loadComponent: () => {
            console.debug('[Shell] Attempting to load remote profile component...');
            const client = AppInjector.get<RemoteClient>(REMOTE_CLIENT);
            const auth = AppInjector.get(AuthService);

            return client.loadRemoteModule(
                'remote-profile',      // Registered name in DB/Governance Service
                './ProfileComponent', // Exposed Key in webpack.config.js
                {
                    type: 'module',
                    context: auth.getContext() // Dynamic Context Injection
                }
            ).then((m: any) => {
                console.debug('[Shell] Remote profile component loaded successfully');
                return m.ProfileComponent;
            }).catch(err => {
                console.error('[Shell] Failed to load remote profile component:', err);
                throw err;
            });
        }
    },
    {
        path: 'live',
        loadComponent: () => import('./live-profile/live-profile.component').then(m => m.LiveProfileComponent)
    }
];
