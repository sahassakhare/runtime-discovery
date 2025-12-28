import { Routes } from '@angular/router';
import { OverviewComponent } from './features/overview/overview.component';
import { DeploymentComponent } from './features/deployment/deployment.component';
import { SettingsComponent } from './features/settings/settings.component';
import { ResolutionGraphComponent } from './features/resolution-graph/resolution-graph.component';

export const routes: Routes = [
    {
        path: '',
        redirectTo: 'overview',
        pathMatch: 'full'
    },
    {
        path: 'overview',
        component: OverviewComponent,
        title: 'Mission Control - Overview'
    },
    {
        path: 'deployments',
        component: DeploymentComponent,
        title: 'Deployments'
    },
    {
        path: 'resolution',
        component: ResolutionGraphComponent, // Using directly for now, might need wrapper if inputs needed
        title: 'Resolution Strategy'
    },
    {
        path: 'settings',
        component: SettingsComponent,
        title: 'Settings'
    },
    // Placeholder for Governance if we had a dedicated component
    {
        path: '**',
        redirectTo: 'overview'
    }
];
