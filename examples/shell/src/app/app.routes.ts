import { Routes } from '@angular/router';
import { loadRemoteModule } from '@maverick/runtime-discovery';
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
        loadComponent: () =>
            loadRemoteModule('12345.myportal.profile', './Profile', { type: 'module' })
                .then((m: any) => m.ProfileComponent)
                .catch(err => {
                    console.error('Failed to load profile remote:', err);
                    throw err;
                })
    },
    {
        path: 'live',
        loadComponent: () => import('./live-profile/live-profile.component').then(m => m.LiveProfileComponent)
    }
];
