import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DashboardService } from '../../core/services/dashboard.service';
import { toSignal } from '@angular/core/rxjs-interop';
import { MatTableModule } from '@angular/material/table';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTooltipModule } from '@angular/material/tooltip';

@Component({
    selector: 'app-runtime',
    standalone: true,
    imports: [CommonModule, MatTableModule, MatIconModule, MatProgressBarModule, MatTooltipModule],
    templateUrl: './runtime.component.html',
    styleUrl: './runtime.component.css'
})
export class RuntimeComponent {
    private dashboardService = inject(DashboardService);
    metrics = toSignal(this.dashboardService.getRuntime());
    displayedColumns: string[] = ['mfeName', 'versionSkew', 'clientErrors', 'latency', 'health'];

    parseFloat(v: string): number {
        return parseFloat(v.replace('%', ''));
    }

    parseInt(v: string): number {
        return parseInt(v.replace('ms', ''));
    }
}
