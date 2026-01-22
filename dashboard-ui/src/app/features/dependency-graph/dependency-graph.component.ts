import { Component, inject, signal, OnInit, ElementRef, ViewChild, AfterViewInit, HostListener, effect, ViewEncapsulation, OnDestroy } from '@angular/core';

import { CommonModule } from '@angular/common';
import { DashboardService } from '../../core/services/dashboard.service';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import * as d3 from 'd3';

interface GraphNode extends d3.SimulationNodeDatum {
  id: string;
  label: string;
  type: 'app' | 'external';
  exposedModules?: string[];
}

interface GraphLink extends d3.SimulationLinkDatum<GraphNode> {
  source: string | GraphNode;
  target: string | GraphNode;
  label: string;
  dynamic: boolean;
}

@Component({
  selector: 'app-dependency-graph',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatIconModule, MatButtonModule, MatProgressSpinnerModule],
  encapsulation: ViewEncapsulation.None,
  template: `
    <div class="page-container">
      <div class="main-content">
        <mat-card class="header-card">
          <mat-card-header>
            <mat-icon mat-card-avatar color="primary">hub</mat-icon>
            <mat-card-title>Polylith Galaxy</mat-card-title>
            <mat-card-subtitle>Interactive Force-Directed Dependency Graph</mat-card-subtitle>
          </mat-card-header>
        </mat-card>

        <div class="graph-viewport" #graphContainer>
           <!-- D3 Graph will be rendered here -->
           <div class="legend">
              <div class="legend-item"><span class="dot app"></span> Application</div>
              <div class="legend-item"><span class="dot external"></span> Remote (Ext)</div>
              <div class="legend-item"><span class="line static"></span> Static</div>
              <div class="legend-item"><span class="line dynamic"></span> Dynamic</div>
           </div>
        </div>
      </div>

      <!-- Detail Panel -->
      <div class="detail-panel" *ngIf="selectedNode() as node">
        <div class="panel-header">
           <div class="panel-title">{{node.label}}</div>
           <button mat-icon-button (click)="selectedNode.set(null)">
             <mat-icon>close</mat-icon>
           </button>
        </div>
        <div class="panel-body">
           <section>
             <div class="section-title">Type</div>
             <div class="section-value">{{node.type | uppercase}}</div>
           </section>

           <section *ngIf="node.exposedModules?.length">
             <div class="section-title">Exposed Modules</div>
             <div class="exposed-list">
               <div class="exposed-item" *ngFor="let mod of node.exposedModules">
                 <mat-icon>extension</mat-icon>
                 <span>{{mod}}</span>
               </div>
             </div>
           </section>

           <section *ngIf="getOutgoingEdges(node) as outgoing">
             <div class="section-title">Consumes</div>
             <div class="exposed-list">
               <div class="exposed-item" *ngFor="let edge of outgoing">
                 <mat-icon>link</mat-icon>
                 <div class="consume-info">
                   <div class="remote-name">{{getTargetId(edge)}}</div>
                   <div class="modules">{{edge.label}}</div>
                 </div>
               </div>
             </div>
           </section>
        </div>
        
        <!-- Version History Section -->
        <div class="panel-body" *ngIf="selectedNodeDetails() as details">
           <section>
             <div class="section-title">Version History</div>
             <div *ngIf="isLoadingDetails()" class="loading-spinner">
                <mat-spinner diameter="20"></mat-spinner>
             </div>
             <div class="version-list" *ngIf="!isLoadingDetails()">
               <div class="version-item" *ngFor="let v of details.versions" [class.locked]="v === details.lockedVersion">
                 <div class="version-info">
                    <span class="v-num">{{v}}</span>
                    <span *ngIf="v === details.activeVersion" class="badge active">Active</span>
                    <span *ngIf="v === details.lockedVersion" class="badge locked">Locked</span>
                 </div>
                 <div class="actions">
                    <button mat-icon-button color="warn" *ngIf="v === details.lockedVersion" (click)="unlockVersion(details.name)">
                        <mat-icon>lock</mat-icon>
                    </button>
                    <button mat-icon-button *ngIf="v !== details.lockedVersion" (click)="lockVersion(details.name, v)">
                        <mat-icon>lock_open</mat-icon>
                    </button>
                 </div>
               </div>
             </div>
           </section>
        </div>
      </div>
    </div>
  `,
  styles: [`
    :host { display: block; height: 100%; width: 100%; }
    .page-container { height: 100%; width: 100%; display: flex; overflow: hidden; background: #0f172a; color: #fff; }
    .main-content { flex: 1; min-width: 0; display: flex; flex-direction: column; padding: 24px; overflow: hidden; position: relative; }
    .header-card { margin-bottom: 24px; border-radius: 16px; background: #1e293b; color: #fff; border: 1px solid #334155; }
    ::ng-deep .mat-mdc-card-title { color: #fff !important; }
    ::ng-deep .mat-mdc-card-subtitle { color: #94a3b8 !important; }

    .graph-viewport { 
        flex: 1 1 auto; /* Grow and shrink, basis auto */
        min-height: 70vh; /* Ensure visibility always */
        width: 100%;
        background: radial-gradient(circle at center, #1e293b 0%, #0f172a 100%); 
        border-radius: 16px; 
        border: 1px solid #334155; 
        overflow: hidden;
        position: relative;
    }

    .legend {
        position: absolute;
        bottom: 20px;
        left: 20px;
        background: rgba(30, 41, 59, 0.8);
        padding: 10px;
        border-radius: 8px;
        border: 1px solid #334155;
        backdrop-filter: blur(4px);
        pointer-events: none;
    }
    .legend-item { display: flex; align-items: center; gap: 8px; font-size: 11px; color: #cbd5e1; margin-bottom: 4px; }
    .dot { width: 8px; height: 8px; border-radius: 50%; }
    .dot.app { background: #3b82f6; box-shadow: 0 0 8px #3b82f6; }
    .dot.external { background: #64748b; }
    .line { width: 20px; height: 2px; }
    .line.static { background: #475569; }
    .line.dynamic { border-top: 2px dashed #3b82f6; height: 0; }

    .detail-panel { width: 320px; background: #1e293b; border-left: 1px solid #334155; display: flex; flex-direction: column; z-index: 10; }
    .panel-header { padding: 20px; border-bottom: 1px solid #334155; display: flex; justify-content: space-between; align-items: center; }
    .panel-title { font-weight: 900; color: #fff; text-transform: uppercase; letter-spacing: 0.1em; font-size: 14px; }
    .panel-body { padding: 20px; overflow-y: auto; flex: 1; }
    
    section { margin-bottom: 24px; }
    .section-title { font-size: 9px; font-weight: 900; color: #94a3b8; text-transform: uppercase; letter-spacing: 0.15em; margin-bottom: 12px; }
    .section-value { font-size: 13px; font-weight: 700; color: #e2e8f0; }

    .exposed-list { display: flex; flex-direction: column; gap: 12px; }
    .exposed-item { display: flex; align-items: flex-start; gap: 12px; }
    .exposed-item mat-icon { font-size: 16px; width: 16px; height: 16px; color: #3b82f6; margin-top: 2px; }
    .exposed-item span { font-size: 13px; font-weight: 600; color: #e2e8f0; }
    
    .consume-info { display: flex; flex-direction: column; gap: 2px; }
    .remote-name { font-size: 13px; font-weight: 700; color: #e2e8f0; }
    .modules { font-size: 10px; color: #94a3b8; font-family: 'JetBrains Mono', monospace; }

    /* D3 Styles */
    text { font-family: 'JetBrains Mono', monospace; font-size: 10px; fill: #94a3b8; pointer-events: none; text-shadow: 0 1px 2px rgba(0,0,0,0.8); }
    .node circle { transition: all 0.3s; stroke: #fff; stroke-width: 0; }
    .node:hover circle { stroke-width: 2px; filter: drop-shadow(0 0 8px rgba(59, 130, 246, 0.8)); }
    line { stroke-opacity: 0.6; transition: stroke-width 0.3s; }

    .version-list { display: flex; flex-direction: column; gap: 8px; }
    .version-item { display: flex; justify-content: space-between; align-items: center; padding: 8px; background: #334155; border-radius: 6px; border: 1px solid transparent; }
    .version-item.locked { border-color: #ef4444; background: rgba(239, 68, 68, 0.1); }
    .version-info { display: flex; align-items: center; gap: 8px; }
    .v-num { font-family: 'JetBrains Mono', monospace; font-size: 12px; color: #fff; }
    .badge { font-size: 9px; padding: 2px 6px; border-radius: 4px; font-weight: 700; text-transform: uppercase; }
    .badge.active { background: #059669; color: #ecfdf5; }
    .badge.locked { background: #dc2626; color: #fef2f2; }
    .loading-spinner { display: flex; justify-content: center; padding: 10px; }
  `]
})
export class DependencyGraphComponent implements OnInit, AfterViewInit, OnDestroy {
  private dashboardService = inject(DashboardService);

  @ViewChild('graphContainer') graphContainer!: ElementRef;

  nodes = signal<GraphNode[]>([]);
  edges = signal<GraphLink[]>([]);
  selectedNode = signal<GraphNode | null>(null);
  selectedNodeDetails = signal<any>(null);
  isLoadingDetails = signal<boolean>(false);

  private svg: any;
  private simulation: any;
  private width = 0;
  private height = 0;

  constructor() {
    effect(() => {
      const n = this.nodes();
      const e = this.edges();
      if (this.simulation && n.length) {
        this.updateGraph(n, e);
      }
    });
  }

  ngOnInit() {
    this.dashboardService.getDependencyGraph().subscribe(data => {
      // Transform data for D3
      const d3Nodes = data.nodes.map((n: any) => ({ ...n }));
      const d3Edges = data.edges.map((e: any) => ({ ...e }));

      this.nodes.set(d3Nodes);
      this.edges.set(d3Edges);
    });
  }

  private resizeObserver: ResizeObserver | undefined;

  ngAfterViewInit() {
    if (!this.graphContainer) {
      console.warn('[DepGraph] graphContainer is undefined in ngAfterViewInit');
      return;
    }

    // Initialize Observer
    this.resizeObserver = new ResizeObserver(entries => {
      for (const entry of entries) {
        if (entry.contentRect.width > 0 && entry.contentRect.height > 0) {
          this.onResize();
        }
      }
    });
    this.resizeObserver.observe(this.graphContainer.nativeElement);

    this.initGraph();

    // Check if we have data waiting
    if (this.nodes().length > 0) {
      this.updateGraph(this.nodes(), this.edges());
    }
  }

  ngOnDestroy() {
    this.resizeObserver?.disconnect();
  }

  onResize() {
    if (!this.graphContainer) return;
    const element = this.graphContainer.nativeElement;
    this.width = element.offsetWidth || 800;
    this.height = element.offsetHeight || 600;

    // Update SVG ViewBox
    d3.select(element).select('svg')
      .attr("viewBox", `0 0 ${this.width} ${this.height}`);

    // Update Simulation Center
    if (this.simulation) {
      this.simulation.force("center", d3.forceCenter(this.width / 2, this.height / 2));
      this.simulation.alpha(0.3).restart();
    }
  }

  // ...

  private initGraph() {
    if (!this.graphContainer) return;
    const element = this.graphContainer.nativeElement;
    this.width = element.offsetWidth || 800; // Fallback to 800 if 0
    this.height = element.offsetHeight || 600; // Fallback to 600 if 0

    // Clear previous if any
    d3.select(element).select('svg').remove();

    const svg = d3.select(element).append("svg")
      .attr("width", "100%")
      .attr("height", "100%")
      .attr("viewBox", `0 0 ${this.width} ${this.height}`);

    const g = svg.append("g");

    // Assign to class property for use in updateGraph
    this.svg = g;

    // Apply zoom
    svg.call(d3.zoom<SVGSVGElement, unknown>().on("zoom", (event) => {
      g.attr("transform", event.transform);
    }));

    // Force Simulation
    this.simulation = d3.forceSimulation()
      .force("link", d3.forceLink().id((d: any) => d.id).distance(150))
      .force("charge", d3.forceManyBody().strength(-400))
      .force("center", d3.forceCenter(this.width / 2, this.height / 2))
      .force("collide", d3.forceCollide().radius(40));
  }

  private updateGraph(nodes: GraphNode[], links: GraphLink[]) {
    const svg = this.svg;

    // Edges
    const link = svg.selectAll(".link")
      .data(links)
      .join("line")
      .attr("class", "link")
      .attr("stroke", (d: any) => d.dynamic ? "#3b82f6" : "#475569")
      .attr("stroke-width", 1.5)
      .attr("stroke-dasharray", (d: any) => d.dynamic ? "4 4" : "none")
      .attr("marker-end", "url(#arrow)");

    // Nodes
    const node = svg.selectAll(".node")
      .data(nodes)
      .join("g")
      .attr("class", "node")
      .call(this.drag(this.simulation))
      .on("click", (event: any, d: GraphNode) => {
        this.onNodeClick(d);
        event.stopPropagation();
      });

    // Node Circles
    node.selectAll("circle").remove(); // Clear previous
    node.append("circle")
      .attr("r", (d: any) => d.type === 'app' ? 20 : 12)
      .attr("fill", (d: any) => d.type === 'app' ? "#3b82f6" : "#64748b")
      .attr("fill-opacity", 0.8)
      .attr("stroke", "#fff")
      .attr("stroke-width", 0);

    // Halo for Apps
    node.filter((d: any) => d.type === 'app')
      .append("circle")
      .attr("r", 25)
      .attr("fill", "transparent")
      .attr("stroke", "#3b82f6")
      .attr("stroke-opacity", 0.3)
      .attr("stroke-width", 1);

    // Labels
    node.selectAll("text").remove();
    node.append("text")
      .attr("dy", (d: any) => d.type === 'app' ? 35 : 25)
      .attr("text-anchor", "middle")
      .text((d: any) => d.label);

    // Arrow Marker
    svg.append("defs").selectAll("marker")
      .data(["arrow"])
      .join("marker")
      .attr("id", "arrow")
      .attr("viewBox", "0 -5 10 10")
      .attr("refX", 28) // Offset to not overlap circle
      .attr("refY", 0)
      .attr("markerWidth", 6)
      .attr("markerHeight", 6)
      .attr("orient", "auto")
      .append("path")
      .attr("fill", "#475569")
      .attr("d", "M0,-5L10,0L0,5");

    // START TICKER
    this.simulation
      .nodes(nodes)
      .on("tick", () => {
        if (this.simulation.alpha() > 0.99) {
        }

        link
          .attr("x1", (d: any) => d.source.x)
          .attr("y1", (d: any) => d.source.y)
          .attr("x2", (d: any) => d.target.x)
          .attr("y2", (d: any) => d.target.y);

        node
          .attr("transform", (d: any) => `translate(${d.x},${d.y})`);
      });

    this.simulation.force("link").links(links);
    this.simulation.alpha(1).restart();
  }

  private drag(simulation: any) {
    function dragstarted(event: any) {
      if (!event.active) simulation.alphaTarget(0.3).restart();
      event.subject.fx = event.subject.x;
      event.subject.fy = event.subject.y;
    }

    function dragged(event: any) {
      event.subject.fx = event.x;
      event.subject.fy = event.y;
    }

    function dragended(event: any) {
      if (!event.active) simulation.alphaTarget(0);
      event.subject.fx = null;
      event.subject.fy = null;
    }

    return d3.drag()
      .on("start", dragstarted)
      .on("drag", dragged)
      .on("end", dragended);
  }

  getOutgoingEdges(node: any) {
    return this.edges().filter(e => {
      // D3 converts source/target to objects, so we handle both cases
      const sourceId = typeof e.source === 'string' ? e.source : (e.source as GraphNode).id;
      return sourceId === node.id;
    });
  }

  getTargetId(edge: any): string {
    const target = edge.target;
    return typeof target === 'string' ? target : (target as GraphNode).id;
  }

  onNodeClick(node: GraphNode) {
    this.selectedNode.set(node);
    this.selectedNodeDetails.set(null); // Reset details

    if (node.type === 'app') {
      this.isLoadingDetails.set(true);
      this.dashboardService.getMfeDetails(node.id).subscribe({
        next: (details) => {
          this.selectedNodeDetails.set(details);
          this.isLoadingDetails.set(false);
        },
        error: (err) => {
          console.error("Failed to fetch MFE details", err);
          this.isLoadingDetails.set(false);
        }
      });
    }
  }

  lockVersion(mfe: string, version: string) {
    this.isLoadingDetails.set(true);
    this.dashboardService.lockVersion(mfe, version, 'PRODUCTION', true).subscribe({
      next: () => {
        // Refresh details
        this.onNodeClick(this.selectedNode()!);
      },
      error: (err) => console.error(err)
    });
  }

  unlockVersion(mfe: string) {
    this.isLoadingDetails.set(true);
    this.dashboardService.lockVersion(mfe, "", 'PRODUCTION', false).subscribe({
      next: () => {
        // Refresh details
        this.onNodeClick(this.selectedNode()!);
      },
      error: (err) => console.error(err)
    });
  }
}
