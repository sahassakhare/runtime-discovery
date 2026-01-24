import { Component, inject, signal, OnInit, ElementRef, ViewChild, AfterViewInit, HostListener, effect, ViewEncapsulation, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DashboardService } from '../../core/services/dashboard.service';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatChipsModule } from '@angular/material/chips';
import { MatTooltipModule } from '@angular/material/tooltip';
import * as d3 from 'd3';

interface GraphNode extends d3.SimulationNodeDatum {
  id: string;
  label: string;
  type: 'app' | 'external';
  exposedModules?: string[];
  status?: 'ONLINE' | 'OFFLINE' | 'UNKNOWN';
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
  imports: [CommonModule, MatCardModule, MatIconModule, MatButtonModule, MatProgressSpinnerModule, MatChipsModule, MatTooltipModule],
  encapsulation: ViewEncapsulation.None,
  template: `
    <div class="graph-root">
      
      <!-- BACKGROUND GRID -->
      <div class="space-grid"></div>

      <!-- HEADER OVERLAY -->
      <div class="header-overlay">
        <div class="header-content">
          <div class="live-indicator">
            <span class="pulse"></span> LIVE
          </div>
          <div class="stats">
            <div class="stat-item">
              <span class="value">{{nodes().length}}</span>
              <span class="label">Nodes</span>
            </div>
            <div class="stat-item">
              <span class="value">{{edges().length}}</span>
              <span class="label">Links</span>
            </div>
          </div>
        </div>
      </div>

      <!-- MAIN VIEWPORT -->
      <div class="graph-viewport" #graphContainer>
         <!-- D3 Graph Rendered Here -->
         
         <!-- EMPTY STATE / LOADING -->
         <div class="center-message" *ngIf="nodes().length === 0 && !error()">
            <mat-spinner diameter="50" color="accent"></mat-spinner>
            <div class="scanning-text">Scanning Federated Mesh...</div>
         </div>

         <!-- ERROR STATE -->
         <div class="center-message error" *ngIf="error()">
            <mat-icon class="large-icon">hub</mat-icon>
            <div class="error-title">Connection Lost</div>
            <div class="error-desc">{{error()}}</div>
            <button mat-flat-button color="warn" (click)="reload()">Retry Discovery</button>
         </div>
      </div>

      <!-- CONTROLS -->
      <div class="controls-overlay">
        <button mat-icon-button (click)="zoomToFit()" matTooltip="Reset View">
          <mat-icon>center_focus_strong</mat-icon>
        </button>
      </div>

      <!-- LEGEND -->
      <div class="legend-glass">
        <div class="legend-row"><span class="dot app"></span> Application</div>
        <div class="legend-row"><span class="dot external"></span> Remote Module</div>
        <div class="legend-row"><span class="line static"></span> Static Dep</div>
        <div class="legend-row"><span class="line dynamic"></span> Dynamic (Runtime)</div>
      </div>

      <!-- DETAILS PANEL (SLIDE OVERS) -->
      <div class="details-panel glass-panel" [class.open]="selectedNode()">
        <div class="panel-header" *ngIf="selectedNode() as node">
          <button mat-icon-button (click)="closeDetails()" class="close-btn"><mat-icon>close</mat-icon></button>
          <div class="node-icon-large" [class]="node.type">
            <mat-icon>{{ node.type === 'app' ? 'web' : 'extension' }}</mat-icon>
          </div>
          <div class="node-titles">
            <div class="node-type">{{ node.type }}</div>
            <div class="node-name">{{ node.label }}</div>
          </div>
        </div>

        <div class="panel-body" *ngIf="selectedNodeDetails(); else loadingDetails">
           <section class="info-section">
              <div class="section-label">Active Version</div>
              <div class="version-display">
                 <span class="v-tag">{{ selectedNodeDetails()?.activeVersion?.version || 'Unknown' }}</span>
                 <span class="env-tag">{{ selectedNodeDetails()?.activeVersion?.environment || 'N/A' }}</span>
              </div>
           </section>

           <section class="info-section" *ngIf="selectedNodeDetails()?.dependencies?.length">
              <div class="section-label">Dependencies</div>
              <div class="chip-grid">
                 <div class="dep-chip" *ngFor="let dep of selectedNodeDetails()?.dependencies">
                    <mat-icon>arrow_forward</mat-icon>
                    {{ dep.remoteName }}
                 </div>
              </div>
           </section>

           <section class="info-section" *ngIf="selectedNodeDetails()?.consumers?.length">
              <div class="section-label">Used By</div>
              <div class="chip-grid">
                 <div class="dep-chip consumer" *ngFor="let consumer of selectedNodeDetails()?.consumers">
                    <mat-icon>arrow_back</mat-icon>
                    {{ consumer }}
                 </div>
              </div>
           </section>
           
           <div class="actions-row">
             <button mat-stroked-button color="primary" class="full-btn">
               <mat-icon>visibility</mat-icon> View Telemetry
             </button>
           </div>
        </div>

        <ng-template #loadingDetails>
           <div class="panel-loading" *ngIf="selectedNode() && isLoadingDetails()">
              <mat-progress-spinner mode="indeterminate" diameter="30"></mat-progress-spinner>
              <span>Fetching metadata...</span>
           </div>
        </ng-template>
      </div>

    </div>
  `,
  styles: [`
    :host { 
        display: block; 
        position: absolute; 
        top: 0; 
        left: 0; 
        right: 0; 
        bottom: 0; 
        overflow: hidden; 
    }

    .graph-root {
        width: 100%;
        height: 100%;
        position: relative;
        background-color: #0f172a;
        overflow: hidden;
    }

    /* Animated Space Grid Background */
    .space-grid {
        position: absolute;
        width: 200%;
        height: 200%;
        top: -50%;
        left: -50%;
        background-image: 
            linear-gradient(rgba(255, 255, 255, 0.03) 1px, transparent 1px),
            linear-gradient(90deg, rgba(255, 255, 255, 0.03) 1px, transparent 1px);
        background-size: 50px 50px;
        transform: perspective(500px) rotateX(60deg);
        animation: grid-move 20s linear infinite;
        pointer-events: none;
        z-index: 0;
    }

    @keyframes grid-move {
        0% { transform: perspective(500px) rotateX(60deg) translateY(0); }
        100% { transform: perspective(500px) rotateX(60deg) translateY(50px); }
    }

    .graph-viewport {
        flex: 1;
        width: 100%;
        height: 100%;
        z-index: 1;
        cursor: grab;
    }
    .graph-viewport:active { cursor: grabbing; }

    /* Glassmorphism Utilities */
    .glass-panel {
        background: rgba(15, 23, 42, 0.7);
        backdrop-filter: blur(12px);
        -webkit-backdrop-filter: blur(12px);
        border: 1px solid rgba(255, 255, 255, 0.1);
        box-shadow: 0 8px 32px rgba(0, 0, 0, 0.4);
    }

    /* Header Overlay */
    .header-overlay {
        position: absolute;
        top: 20px;
        left: 20px;
        z-index: 10;
        pointer-events: none;
    }
    .header-content {
        display: flex;
        align-items: center;
        gap: 20px;
    }
    .live-indicator {
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 11px;
        font-weight: 800;
        color: #10b981;
        background: rgba(16, 185, 129, 0.1);
        padding: 4px 10px;
        border-radius: 20px;
        border: 1px solid rgba(16, 185, 129, 0.2);
    }
    .pulse {
        width: 6px;
        height: 6px;
        background: #10b981;
        border-radius: 50%;
        box-shadow: 0 0 0 0 rgba(16, 185, 129, 0.7);
        animation: pulse-green 2s infinite;
    }
    @keyframes pulse-green {
        0% { box-shadow: 0 0 0 0 rgba(16, 185, 129, 0.7); }
        70% { box-shadow: 0 0 0 6px rgba(16, 185, 129, 0); }
        100% { box-shadow: 0 0 0 0 rgba(16, 185, 129, 0); }
    }
    .stats {
        display: flex;
        gap: 15px;
    }
    .stat-item {
        display: flex;
        flex-direction: column;
    }
    .stat-item .value { font-size: 18px; font-weight: 700; color: white; line-height: 1; }
    .stat-item .label { font-size: 10px; text-transform: uppercase; color: #94a3b8; font-weight: 600; }

    /* Controls */
    .controls-overlay {
        position: absolute;
        bottom: 30px;
        right: 30px;
        z-index: 10;
        display: flex;
        flex-direction: column;
        gap: 10px;
    }
    .controls-overlay button {
        background: rgba(30, 41, 59, 0.9);
        color: white;
        border: 1px solid rgba(255,255,255,0.1);
    }

    /* Legend */
    .legend-glass {
        position: absolute;
        bottom: 30px;
        left: 30px;
        padding: 15px;
        border-radius: 12px;
        background: rgba(15, 23, 42, 0.8);
        backdrop-filter: blur(8px);
        border: 1px solid rgba(255,255,255,0.05);
        z-index: 5;
        pointer-events: none;
    }
    .legend-row {
        display: flex;
        align-items: center;
        gap: 10px;
        font-size: 12px;
        color: #cbd5e1;
        margin-bottom: 6px;
    }
    .legend-row:last-child { margin-bottom: 0; }
    .dot { width: 8px; height: 8px; border-radius: 50%; }
    .dot.app { background: #3b82f6; box-shadow: 0 0 8px #3b82f6; }
    .dot.external { background: #64748b; }
    .line { width: 20px; height: 2px; background: #475569; }
    .line.dynamic { border-top: 2px dashed #3b82f6; background: transparent; }

    /* Overlay Messages */
    .center-message {
        position: absolute;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 15px;
        color: #94a3b8;
        pointer-events: none;
        text-align: center;
    }
    .scanning-text {
        font-family: 'JetBrains Mono', monospace;
        font-size: 12px;
        letter-spacing: 2px;
        text-transform: uppercase;
        animation: blink 1.5s infinite;
    }
    .error .error-title { color: #f43f5e; font-size: 18px; font-weight: 700; }
    .error .error-desc { max-width: 300px; color: #94a3b8; font-size: 13px; line-height: 1.4; margin-bottom: 10px; }
    .error button { pointer-events: auto; }

    /* Details Panel */
    .details-panel {
        position: absolute;
        top: 0;
        right: 0;
        bottom: 0;
        width: 360px;
        border-left: 1px solid rgba(255,255,255,0.1);
        transform: translateX(100%);
        transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        z-index: 20;
        display: flex;
        flex-direction: column;
    }
    .details-panel.open { transform: translateX(0); }

    .panel-header {
        padding: 24px;
        border-bottom: 1px solid rgba(255,255,255,0.05);
        display: flex;
        flex-direction: column;
        align-items: center;
        text-align: center;
        position: relative;
    }
    .close-btn { position: absolute !important; top: 10px; right: 10px; color: #64748b; }
    
    .node-icon-large {
        width: 64px;
        height: 64px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-bottom: 15px;
        background: linear-gradient(135deg, #1e293b, #0f172a);
        border: 1px solid rgba(255,255,255,0.1);
    }
    .node-icon-large mat-icon { font-size: 32px; width: 32px; height: 32px; color: #94a3b8; }
    .node-icon-large.app { box-shadow: 0 0 30px rgba(59, 130, 246, 0.2); border-color: rgba(59, 130, 246, 0.3); }
    .node-icon-large.app mat-icon { color: #60a5fa; }

    .node-titles .node-type { font-size: 10px; text-transform: uppercase; letter-spacing: 1px; color: #64748b; font-weight: 700; margin-bottom: 4px; }
    .node-titles .node-name { font-size: 20px; font-weight: 600; color: white; }

    .panel-body { padding: 24px; flex: 1; overflow-y: auto; color: #cbd5e1; }
    .info-section { margin-bottom: 24px; }
    .section-label { font-size: 11px; text-transform: uppercase; font-weight: 700; color: #475569; margin-bottom: 10px; letter-spacing: 0.5px; }
    
    .version-display {
        display: flex;
        gap: 10px;
        align-items: center;
    }
    .v-tag { font-family: 'JetBrains Mono', monospace; font-size: 14px; color: white; background: rgba(255,255,255,0.05); padding: 4px 8px; border-radius: 6px; border: 1px solid rgba(255,255,255,0.1); }
    .env-tag { font-size: 10px; font-weight: 700; background: #064e3b; color: #34d399; padding: 4px 8px; border-radius: 4px; border: 1px solid rgba(16, 185, 129, 0.2); }

    .chip-grid { display: flex; flex-wrap: wrap; gap: 8px; }
    .dep-chip {
        display: flex;
        align-items: center;
        gap: 6px;
        background: rgba(30, 41, 59, 0.5);
        border: 1px solid rgba(255,255,255,0.05);
        padding: 6px 10px;
        border-radius: 6px;
        font-size: 12px;
        color: #e2e8f0;
    }
    .dep-chip mat-icon { font-size: 14px; width: 14px; height: 14px; color: #64748b; }
    .dep-chip.consumer mat-icon { color: #818cf8; }

    .full-btn { width: 100%; border-radius: 8px; height: 44px; }
    .actions-row { margin-top: auto; padding-top: 20px; border-top: 1px solid rgba(255,255,255,0.05); }
    
    .panel-loading { display: flex; flex-direction: column; align-items: center; gap: 10px; color: #64748b; padding-top: 40px; }

    /* SVG Elements */
    .node-label { font-family: 'Inter', sans-serif; font-size: 10px; font-weight: 500; fill: #94a3b8; pointer-events: none; text-shadow: 0 2px 4px rgba(0,0,0,0.8); transition: opacity 0.2s; opacity: 0.8; }
    .node:hover .node-label { opacity: 1; fill: white; font-weight: 700; }
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
  error = signal<string | null>(null);

  private svg: any;
  private g: any;
  private simulation: any;
  private zoom: any;
  private width = 0;
  private height = 0;
  private resizeObserver: ResizeObserver | undefined;

  ngOnInit() {
    this.reload();
  }

  ngAfterViewInit() {
    if (!this.graphContainer) return;

    // Use debounce to prevent loop
    let resizeTimer: any;
    this.resizeObserver = new ResizeObserver((entries) => {
      if (!entries || entries.length === 0) return;
      const rect = entries[0].contentRect;

      // Epsilon check: Ignore small changes (< 2px)
      if (Math.abs(rect.width - this.width) < 2 && Math.abs(rect.height - this.height) < 2) return;

      // Debounce
      cancelAnimationFrame(resizeTimer);
      resizeTimer = requestAnimationFrame(() => {
        this.width = rect.width;
        this.height = rect.height;
        this.onResize();
      });
    });
    this.resizeObserver.observe(this.graphContainer.nativeElement);

    // Initial Setup
    this.initGraph();
  }

  ngOnDestroy() {
    this.resizeObserver?.disconnect();
    if (this.simulation) this.simulation.stop();
  }

  reload() {
    this.error.set(null);
    this.dashboardService.getDependencyGraph().subscribe({
      next: (data) => {
        // Correct transform for D3 mutability
        const d3Nodes = data.nodes.map((n: any) => ({ ...n }));
        const d3Edges = data.edges.map((e: any) => ({ ...e }));

        console.log('[Graph] Loaded:', d3Nodes.length, 'nodes', d3Edges.length, 'edges');

        this.nodes.set(d3Nodes);
        this.edges.set(d3Edges);
        this.updateGraph(d3Nodes, d3Edges);

        // Auto-center after a slight delay to allow simulation to expand
        setTimeout(() => this.zoomToFit(), 500);
      },
      error: (err) => {
        console.error('Graph Load Error:', err);
        this.error.set("Unable to fetch topology from Registry Service.");
      }
    });
  }

  onResize() {
    if (!this.svg || this.width === 0 || this.height === 0) return;

    // Log removed to prevent spam during layout settle
    this.svg.attr("viewBox", [0, 0, this.width, this.height]);

    if (this.simulation) {
      this.simulation.force("center", d3.forceCenter(this.width / 2, this.height / 2));
      this.simulation.alpha(0.5).restart();
    }
  }

  zoomToFit() {
    if (!this.svg || !this.g || this.nodes().length === 0) return;

    // Allow simulation to settle a bit before measuring bounds
    // But if we need immediate feedback, we calculate current positions
    setTimeout(() => {
      try {
        const bounds = this.g.node().getBBox();
        if (bounds.width === 0 || bounds.height === 0) return; // Nothing to zoom to

        const parent = this.graphContainer.nativeElement;
        const fullWidth = parent.clientWidth || 800;
        const fullHeight = parent.clientHeight || 600;

        const width = Math.max(bounds.width, 100);
        const height = Math.max(bounds.height, 100);
        const midX = bounds.x + bounds.width / 2;
        const midY = bounds.y + bounds.height / 2;

        const scale = 0.85 / Math.max(width / fullWidth, height / fullHeight);
        const zoomScale = Math.min(Math.max(scale, 0.2), 2); // Clamp

        const translate = [fullWidth / 2 - zoomScale * midX, fullHeight / 2 - zoomScale * midY];

        console.log('[Graph] Zooming to:', translate, zoomScale);

        this.svg.transition().duration(750).call(
          this.zoom.transform,
          d3.zoomIdentity.translate(translate[0], translate[1]).scale(zoomScale)
        );
      } catch (e) {
        console.warn("Zoom Fit Failed", e);
      }
    }, 100);
  }

  closeDetails() {
    this.selectedNode.set(null);
  }

  private initGraph() {
    const el = this.graphContainer.nativeElement;
    d3.select(el).select('svg').remove(); // Clear

    this.svg = d3.select(el).append("svg")
      .attr("width", "100%")
      .attr("height", "100%")
      .attr("viewBox", [0, 0, el.clientWidth, el.clientHeight]);

    const g = this.svg.append("g");
    this.g = g;

    // Zoom Behavior
    this.zoom = d3.zoom()
      .scaleExtent([0.1, 4])
      .on("zoom", (event) => g.attr("transform", event.transform));
    this.svg.call(this.zoom);

    // Definitions (Gradients/Markers)
    const defs = this.svg.append("defs");

    // Arrow Marker
    defs.append("marker")
      .attr("id", "arrow-dynamic")
      .attr("viewBox", "0 -5 10 10")
      .attr("refX", 24)
      .attr("refY", 0)
      .attr("markerWidth", 6)
      .attr("markerHeight", 6)
      .attr("orient", "auto")
      .append("path")
      .attr("fill", "#60a5fa") // Blue
      .attr("d", "M0,-5L10,0L0,5");

    defs.append("marker")
      .attr("id", "arrow-static")
      .attr("viewBox", "0 -5 10 10")
      .attr("refX", 20)
      .attr("refY", 0)
      .attr("markerWidth", 6)
      .attr("markerHeight", 6)
      .attr("orient", "auto")
      .append("path")
      .attr("fill", "#475569") // Slate
      .attr("d", "M0,-5L10,0L0,5");

    // Glow Filter
    const filter = defs.append("filter").attr("id", "glow");
    filter.append("feGaussianBlur").attr("stdDeviation", "2.5").attr("result", "coloredBlur");
    const merge = filter.append("feMerge");
    merge.append("feMergeNode").attr("in", "coloredBlur");
    merge.append("feMergeNode").attr("in", "SourceGraphic");

    this.simulation = d3.forceSimulation()
      .force("link", d3.forceLink().id((d: any) => d.id).distance(150))
      .force("charge", d3.forceManyBody().strength(-400))
      .force("collide", d3.forceCollide(40))
      .force("center", d3.forceCenter(el.clientWidth / 2, el.clientHeight / 2));
  }

  private updateGraph(nodes: GraphNode[], links: GraphLink[]) {
    // LINKS
    const link = this.g.selectAll(".link")
      .data(links)
      .join("path") // Using path for curved lines potential
      .attr("class", "link")
      .attr("fill", "none")
      .attr("stroke", (d: any) => d.dynamic ? "#3b82f6" : "#334155")
      .attr("stroke-width", (d: any) => d.dynamic ? 2 : 1)
      .attr("stroke-dasharray", (d: any) => d.dynamic ? "4,4" : "none")
      .attr("marker-end", (d: any) => d.dynamic ? "url(#arrow-dynamic)" : "url(#arrow-static)");

    // Define Node Groups
    const node = this.g.selectAll(".node")
      .data(nodes)
      .join("g")
      .attr("class", "node")
      .attr("cursor", "pointer")
      .call(d3.drag()
        .on("start", (event, d: any) => {
          if (!event.active) this.simulation.alphaTarget(0.3).restart();
          d.fx = d.x; d.fy = d.y;
        })
        .on("drag", (event, d: any) => {
          d.fx = event.x; d.fy = event.y;
        })
        .on("end", (event, d: any) => {
          if (!event.active) this.simulation.alphaTarget(0);
          d.fx = null; d.fy = null;
        }));

    // Circles
    node.selectAll("circle").remove();

    // Outer Glow (Apps)
    node.filter((d: any) => d.type === 'app')
      .append("circle")
      .attr("r", 20)
      .attr("fill", "#3b82f6")
      .attr("opacity", 0.2)
      .append("animate")
      .attr("attributeName", "r")
      .attr("values", "20;25;20")
      .attr("dur", "3s")
      .attr("repeatCount", "indefinite");

    // Main Circle
    node.append("circle")
      .attr("r", (d: any) => d.type === 'app' ? 12 : 8)
      .attr("fill", (d: any) => d.type === 'app' ? "#60a5fa" : "#94a3b8")
      .attr("stroke", "#0f172a")
      .attr("stroke-width", 2)
      .attr("filter", (d: any) => d.type === 'app' ? "url(#glow)" : "");

    // Labels
    node.selectAll("text").remove();
    node.append("text")
      .attr("class", "node-label")
      .attr("dy", 24)
      .attr("text-anchor", "middle")
      .text((d: any) => d.label);

    // Event Handling
    node.on("click", (event: any, d: GraphNode) => {
      event.stopPropagation();
      this.onNodeClick(d);
    });

    // Simulation Tick
    this.simulation.nodes(nodes).on("tick", () => {
      link.attr("d", (d: any) => `M${d.source.x},${d.source.y} L${d.target.x},${d.target.y}`);
      node.attr("transform", (d: any) => `translate(${d.x},${d.y})`);
    });

    this.simulation.force("link").links(links);
    this.simulation.alpha(1).restart();
  }

  onNodeClick(node: GraphNode) {
    this.selectedNode.set(node);
    this.selectedNodeDetails.set(null);
    this.isLoadingDetails.set(true);

    if (node.type === 'app') {
      this.dashboardService.getMfeDetails(node.id).subscribe({
        next: (data) => {
          this.selectedNodeDetails.set(data);
          this.isLoadingDetails.set(false);
        },
        error: (e) => {
          console.warn(e);
          this.isLoadingDetails.set(false);
          // Fallback for demo
          this.selectedNodeDetails.set({
            activeVersion: { version: 'v1.0.0', environment: 'PRODUCTION' },
            dependencies: [],
            consumers: []
          });
        }
      });
    } else {
      this.isLoadingDetails.set(false);
    }
  }
}
