import { Routes } from '@angular/router';
import { OverviewComponent } from './features/overview/overview.component';
import { DeploymentComponent } from './features/deployment/deployment.component';
import { SettingsComponent } from './features/settings/settings.component';
import { ResolutionGraphComponent } from './features/resolution-graph/resolution-graph.component';
import { RuntimeComponent } from './features/runtime/runtime.component';
import { EcosystemPulseComponent } from './features/ecosystem-pulse/ecosystem-pulse.component';
import { DependencyGraphComponent } from './features/dependency-graph/dependency-graph.component';

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
        path: 'runtime',
        component: RuntimeComponent,
        title: 'Runtime Metrics'
    },
    {
        path: 'ecosystem',
        component: EcosystemPulseComponent,
        title: 'Ecosystem Pulse'
    },
    {
        path: 'dependencies',
        component: DependencyGraphComponent,
        title: 'Dependency Graph'
    },
    {
        path: 'settings',
        component: SettingsComponent,
        title: 'Settings'
    },
    {
        path: 'governance',
        loadComponent: () => import('./features/governance/governance.component').then(m => m.GovernanceComponent),
        title: 'Governance'
    },
    {
        path: 'agent',
        loadComponent: () => import('./features/agent/agent-page.component').then(m => m.AgentPageComponent),
        title: 'AI Agent'
    },
    {
        path: '**',
        redirectTo: 'overview'
    }
];
