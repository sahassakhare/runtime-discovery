import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DashboardService } from '../../core/services/dashboard.service';
import { toSignal } from '@angular/core/rxjs-interop';

@Component({
    selector: 'app-runtime',
    standalone: true,
    imports: [CommonModule],
    templateUrl: './runtime.component.html',
    styleUrl: './runtime.component.css'
})
export class RuntimeComponent {
    private dashboardService = inject(DashboardService);
    metrics = toSignal(this.dashboardService.getRuntime());
}
