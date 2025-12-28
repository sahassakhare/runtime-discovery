import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DashboardService } from '../../core/services/dashboard.service';
import { toSignal } from '@angular/core/rxjs-interop';
import { DeploymentComponent } from '../deployment/deployment.component';
import { ResolutionGraphComponent } from '../resolution-graph/resolution-graph.component';
import { RuntimeComponent } from '../runtime/runtime.component';
import { SkeletonComponent } from '../../core/components/skeleton/skeleton.component';

@Component({
    selector: 'app-overview',
    standalone: true,
    imports: [CommonModule, DeploymentComponent, ResolutionGraphComponent, RuntimeComponent, SkeletonComponent],
    templateUrl: './overview.component.html',
    styleUrl: './overview.component.css'
})
export class OverviewComponent {
    private dashboardService = inject(DashboardService);

    stats = toSignal(this.dashboardService.getStats());
    governance = toSignal(this.dashboardService.getGovernance());
}
