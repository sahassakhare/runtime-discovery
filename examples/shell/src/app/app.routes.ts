import { Routes } from '@angular/router';
import { AppInjector } from './app-injector';
import { REMOTE_CLIENT, RemoteClient } from '@maverick/runtime-discovery';
import { loadRemoteModule } from '@angular-architects/module-federation';

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
            return client.loadRemoteModule(
                'remote-profile',      // Registered name in DB/Governance Service
                './ProfileComponent', // Exposed Key in webpack.config.js
                { type: 'module' } // Options
            ).then((m: any) => {
                console.debug('[Shell] Remote profile component loaded successfully');
                return m.ProfileComponent;
            }).catch(err => {
                console.error('[Shell] Failed to load remote profile component:', err);
                throw err;
            });
        }
    }
];
