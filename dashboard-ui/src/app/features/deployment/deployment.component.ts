import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DashboardService } from '../../core/services/dashboard.service';
import { toSignal } from '@angular/core/rxjs-interop';

@Component({
    selector: 'app-deployment',
    standalone: true,
    imports: [CommonModule],
    templateUrl: './deployment.component.html',
    styleUrl: './deployment.component.css'
})
export class DeploymentComponent {
    private dashboardService = inject(DashboardService);
    deployments = toSignal(this.dashboardService.getDeployments());
}
