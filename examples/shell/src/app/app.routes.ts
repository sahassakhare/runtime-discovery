import { Routes } from '@angular/router';
import { AppInjector } from './app-injector';
import { REMOTE_CLIENT } from './core/runtime-discovery/tokens';
import { RemoteClient } from './core/runtime-discovery/remote-client';
import { loadRemoteModule } from '@angular-architects/module-federation';
import { AuthService } from './auth.service';
import { LiveProfileComponent } from './live-profile/live-profile.component';

export const routes: Routes = [
    {
        path: 'agent',
        loadComponent: () => import('./features/agent/agent-page.component').then(m => m.AgentPageComponent)
    },
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
                'remote-profile',
                './Profile', // Reverting to Standard Angular Component for Shell Demo
                {
                    type: 'module',
                    context: auth.getContext()
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
