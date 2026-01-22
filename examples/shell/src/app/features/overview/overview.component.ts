import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DashboardService } from '../../core/services/dashboard.service';
import { toSignal } from '@angular/core/rxjs-interop';
import { DeploymentComponent } from '../deployment/deployment.component';
import { ResolutionGraphComponent } from '../resolution-graph/resolution-graph.component';
import { RuntimeComponent } from '../runtime/runtime.component';
import { SkeletonComponent } from '../../core/components/skeleton/skeleton.component';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatChipsModule } from '@angular/material/chips';
import { MatListModule } from '@angular/material/list';

@Component({
    selector: 'app-overview',
    standalone: true,
    imports: [
        CommonModule,
        DeploymentComponent,
        ResolutionGraphComponent,
        RuntimeComponent,
        SkeletonComponent,
        MatCardModule,
        MatIconModule,
        MatDividerModule,
        MatProgressBarModule,
        MatChipsModule,
        MatListModule
    ],
    templateUrl: './overview.component.html',
    styleUrl: './overview.component.css'
})
export class OverviewComponent {
    private dashboardService = inject(DashboardService);

    stats = toSignal(this.dashboardService.getStats());
    governance = toSignal(this.dashboardService.getGovernance());
}
