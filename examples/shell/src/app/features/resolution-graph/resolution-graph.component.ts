import { Component, inject, Input, signal, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DashboardService } from '../../core/services/dashboard.service';
import { ResolutionGraph } from '../../core/models/dashboard.model';

@Component({
    selector: 'app-resolution-graph',
    standalone: true,
    imports: [CommonModule],
    templateUrl: './resolution-graph.component.html',
    styleUrl: './resolution-graph.component.css'
})
export class ResolutionGraphComponent implements OnChanges {
    @Input() remoteName: string = 'remote-profile'; // Default for demo
    private dashboardService = inject(DashboardService);

    graphData = signal<ResolutionGraph | null>(null);

    ngOnChanges(changes: SimpleChanges): void {
        this.loadGraph();
    }

    loadGraph() {
        this.dashboardService.getResolutionGraph(this.remoteName).subscribe(data => {
            this.graphData.set(data);
        });
    }

    get versionNodes() {
        return this.graphData()?.nodes.filter(n => n.type.includes('version')) || [];
    }
}
