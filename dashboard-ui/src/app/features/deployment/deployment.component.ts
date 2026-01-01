import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DashboardService } from '../../core/services/dashboard.service';
import { toSignal } from '@angular/core/rxjs-interop';
import { MatTableModule } from '@angular/material/table';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatButtonModule } from '@angular/material/button';

@Component({
    selector: 'app-deployment',
    standalone: true,
    imports: [CommonModule, MatTableModule, MatIconModule, MatChipsModule, MatButtonModule],
    templateUrl: './deployment.component.html',
    styleUrl: './deployment.component.css'
})
export class DeploymentComponent {
    private dashboardService = inject(DashboardService);
    deployments = toSignal(this.dashboardService.getDeployments());
    displayedColumns: string[] = ['name', 'type', 'group', 'activeVersion', 'status', 'actions'];
}
