import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule, NgFor, NgIf } from '@angular/common';
import { MatTabsModule } from '@angular/material/tabs';
import { DashboardService } from '../../core/services/dashboard.service';
import { HoneycombVisualizerComponent, HoneycombNode } from './honeycomb-visualizer.component';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatDividerModule } from '@angular/material/divider';
import { trigger, transition, style, animate } from '@angular/animations';

@Component({
  selector: 'app-ecosystem-pulse',
  standalone: true,
  imports: [CommonModule, HoneycombVisualizerComponent, MatCardModule, MatIconModule, MatButtonModule, MatDividerModule, MatTabsModule],
  animations: [
    trigger('slideInOut', [
      transition(':enter', [
        style({ transform: 'translateX(100%)' }),
        animate('300ms ease-out', style({ transform: 'translateX(0)' }))
      ]),
      transition(':leave', [
        animate('300ms ease-in', style({ transform: 'translateX(100%)' }))
      ])
    ])
  ],
  template: `
    <div class="page-layout">
      <div class="content-main">
        <mat-card class="dashboard-header">
          <mat-card-header>
            <mat-icon mat-card-avatar color="primary">hub</mat-icon>
            <mat-card-title>Ecosystem Pulse</mat-card-title>
            <mat-card-subtitle>Live visualization of registered and healthy microfrontends</mat-card-subtitle>
          </mat-card-header>
        </mat-card>

        <!-- Wallboard Metrics -->
        <div class="metrics-grid">
            <mat-card class="metric-card">
               <div class="metric-value">{{stats()?.totalMfes || 0}}</div>
               <div class="metric-label">Total Applications</div>
            </mat-card>
            <mat-card class="metric-card up">
               <div class="metric-value">{{stats()?.upCount || 0}}</div>
               <div class="metric-label">Instances UP</div>
            </mat-card>
             <mat-card class="metric-card down">
               <div class="metric-value">{{stats()?.downCount || 0}}</div>
               <div class="metric-label">Instances DOWN</div>
            </mat-card>
             <mat-card class="metric-card">
               <div class="metric-value">{{stats()?.avgLighthouseScore || 0}}</div>
               <div class="metric-label">Avg Health Score</div>
            </mat-card>
        </div>

        <app-honeycomb-visualizer 
          [nodes]="mfeNodes()" 
          (nodeClick)="onNodeSelect($event)">
        </app-honeycomb-visualizer>
      </div>

      <!-- Detail Panel -->
      <div *ngIf="selectedMfe(); let selected" class="detail-panel" @slideInOut>
        <div class="panel-header">
          <div class="title-area">
             <h2>{{selected.name}}</h2>
             <span class="status-badge" [class]="selected.status.toLowerCase()">{{selected.status}}</span>
          </div>
          <button mat-icon-button (click)="selectedMfe.set(null)">
            <mat-icon>close</mat-icon>
          </button>
        </div>
        
        <mat-divider></mat-divider>

        <div class="panel-content" *ngIf="details() as d">
          <mat-tab-group>
             <mat-tab label="Details">
                <div class="tab-content">
                  <section>
                    <h3>Metadata</h3>
                    <div class="meta-item" *ngFor="let m of d.metadata">
                       <span class="key">{{m.name}}</span>
                       <span class="value">{{m.value}}</span>
                    </div>
                  </section>

                  <section>
                    <h3>Exposed Modules</h3>
                    <div class="module-item" *ngFor="let m of d.exposedModules">
                       <mat-icon>extension</mat-icon>
                       <span>{{m.name}}</span>
                       <small>{{m.filePath}}</small>
                    </div>
                  </section>

                  <section>
                    <h3>Dependencies</h3>
                    <div class="module-item" *ngFor="let m of d.consumedRemotes">
                       <mat-icon>link</mat-icon>
                       <span>{{m.remoteName}}</span>
                       <small *ngIf="m.dynamic">(Dynamic)</small>
                    </div>
                  </section>
                </div>
             </mat-tab>
             
             <mat-tab label="Metrics">
                <div class="tab-content">
                   <div *ngIf="metrics() as m; else loadingMetrics">
                      <div class="meta-item"><span class="key">Mem Used</span><span class="value">{{m.mem}} MB</span></div>
                      <div class="meta-item"><span class="key">Uptime</span><span class="value">{{(m.uptime / 1000).toFixed(0)}} s</span></div>
                      <div class="meta-item"><span class="key">System Load</span><span class="value">{{m['systemload.average']}}</span></div>
                      <div class="meta-item"><span class="key">Heap Used</span><span class="value">{{m['heap.used']}} MB</span></div>
                      <div class="meta-item"><span class="key">Threads Peak</span><span class="value">{{m['threads.peak']}}</span></div>
                      <div class="meta-item"><span class="key">Classes</span><span class="value">{{m.classes}}</span></div>
                   </div>
                   <ng-template #loadingMetrics>Loading metrics...</ng-template>
                </div>
             </mat-tab>

             <mat-tab label="Environment">
                <div class="tab-content">
                   <div *ngIf="env() as e; else loadingEnv">
                      <section>
                         <h3>Active Profiles</h3>
                         <div class="chip-list">
                            <span class="chip" *ngFor="let p of e.activeProfiles">{{p}}</span>
                         </div>
                      </section>
                      <section *ngFor="let src of e.propertySources">
                         <h3>{{src.name}}</h3>
                         <div class="meta-item" *ngFor="let prop of src.properties | keyvalue">
                            <span class="key">{{prop.key}}</span>
                            <span class="value">{{$any(prop.value).value}}</span>
                         </div>
                      </section>
                   </div>
                   <ng-template #loadingEnv>Loading environment...</ng-template>
                </div>
             </mat-tab>
             
             <mat-tab label="Logs">
                 <div class="tab-content log-viewport">
                    <div *ngIf="logs() as l; else loadingLogs">
                        <div class="log-line" *ngFor="let line of l">{{line}}</div>
                    </div>
                    <ng-template #loadingLogs>Loading logs...</ng-template>
                 </div>
             </mat-tab>

             <mat-tab label="Threads">
                 <div class="tab-content">
                    <div *ngIf="threads() as t; else loadingThreads">
                        <div class="thread-item" *ngFor="let th of t">
                            <div class="thread-header">
                                <span class="thread-id">#{{th.id}}</span>
                                <span class="thread-name">{{th.name}}</span>
                            </div>
                            <div class="thread-state" [class]="th.state.toLowerCase()">{{th.state}}</div>
                        </div>
                    </div>
                    <ng-template #loadingThreads>Loading threads...</ng-template>
                 </div>
             </mat-tab>

             <mat-tab label="Traces">
                 <div class="tab-content">
                    <div *ngIf="traces() as tr; else loadingTraces">
                        <table class="trace-table">
                            <thead><tr><th>Time</th><th>Method</th><th>Status</th><th>URI</th></tr></thead>
                            <tbody>
                                <tr *ngFor="let r of tr">
                                    <td>{{r.timestamp | date:'HH:mm:ss'}}</td>
                                    <td><span class="method-badge">{{r.method}}</span></td>
                                    <td><span class="status-badge" [class.error]="r.status >= 400" [class.success]="r.status < 400">{{r.status}}</span></td>
                                    <td class="uri">...{{r.uri | slice:-20}}</td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                    <ng-template #loadingTraces>Loading traces...</ng-template>
                 </div>
             </mat-tab>
          </mat-tab-group>
        </div>
        
        <div class="panel-content loading" *ngIf="!details()">
          Loading details...
        </div>
      </div>
    </div>
  `,
  styles: [`
    .page-layout { display: flex; height: 100%; position: relative; overflow: hidden; }
    .content-main { flex: 1; overflow-y: auto; padding: 20px; }
    .dashboard-header { margin-bottom: 20px; border-radius: 12px; }

    .detail-panel {
      position: absolute;
      right: 0;
      top: 0;
      width: 450px;
      height: 100%;
      background: #1e1e1e;
      box-shadow: -5px 0 15px rgba(0,0,0,0.5);
      z-index: 100;
      display: flex;
      flex-direction: column;
      color: #e0e0e0;
      padding: 0;
    }

    .panel-header { padding: 20px; display: flex; justify-content: space-between; align-items: flex-start; }
    .title-area h2 { margin: 0; font-size: 24px; color: #fff; }
    .status-badge { font-size: 10px; text-transform: uppercase; padding: 2px 8px; border-radius: 4px; margin-top: 5px; display: inline-block; }
    .status-badge.healthy { background: #2e7d32; }
    .status-badge.live { background: #1565c0; }
    .status-badge.registered { background: #616161; }

    .panel-content { padding: 20px; overflow-y: auto; flex: 1; }
    .panel-content h3 { font-size: 14px; text-transform: uppercase; color: #888; margin-bottom: 15px; border-bottom: 1px solid #333; padding-bottom: 5px; }

    .meta-item { display: flex; justify-content: space-between; margin-bottom: 10px; font-size: 13px; }
    .meta-item .key { color: #aaa; }
    .meta-item .value { color: #fff; font-family: monospace; }

    .module-item { display: flex; align-items: center; gap: 10px; margin-bottom: 12px; }
    .module-item span { font-weight: 500; }
    .module-item small { color: #666; margin-left: auto; }
    .module-item mat-icon { font-size: 18px; width: 18px; height: 18px; color: #03a9f4; }
    
    .metrics-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px; margin-bottom: 24px; }
    .metric-card { padding: 20px; display: flex; flex-direction: column; align-items: center; justify-content: center; background: #2d2d2d; color: #fff; }
    .metric-value { font-size: 32px; font-weight: 700; margin-bottom: 5px; }
    .metric-label { font-size: 12px; text-transform: uppercase; letter-spacing: 1px; color: #aaa; }
    .metric-card.up .metric-value { color: #4caf50; }
    .metric-card.down .metric-value { color: #f44336; }
    
    .tab-content { padding: 20px 0; }
    .chip-list { display: flex; gap: 8px; flex-wrap: wrap; }
    .chip { background: #333; padding: 4px 8px; border-radius: 12px; font-size: 12px; border: 1px solid #444; }

    .log-viewport { background: #000; font-family: 'JetBrains Mono', monospace; font-size: 11px; padding: 10px; color: #bbb; max-height: 400px; overflow-y: auto; border-radius: 4px; }
    .log-line { border-bottom: 1px solid #222; padding: 2px 0; white-space: nowrap; }

    .thread-item { display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid #333; padding: 8px 0; }
    .thread-id { color: #888; font-size: 10px; margin-right: 8px; }
    .thread-name { font-size: 12px; color: #eee; }
    .thread-state { font-size: 9px; padding: 2px 6px; border-radius: 4px; background: #444; }
    .thread-state.runnable { background: #2e7d32; }
    .thread-state.blocked { background: #c62828; }
    .thread-state.waiting { background: #ff8f00; color: #000; }

    .trace-table { width: 100%; border-collapse: collapse; font-size: 11px; }
    .trace-table th { text-align: left; color: #888; border-bottom: 1px solid #444; padding: 5px; }
    .trace-table td { border-bottom: 1px solid #333; padding: 5px; color: #ddd; }
    .method-badge { font-weight: 700; color: #bb86fc; }
    .status-badge.error { color: #cf6679; }
    .status-badge.success { color: #03dac6; }
    .uri { font-family: monospace; color: #aaa; }
  `]
})
export class EcosystemPulseComponent implements OnInit {
  private dashboardService = inject(DashboardService);

  mfeNodes = signal<HoneycombNode[]>([]);
  selectedMfe = signal<HoneycombNode | null>(null);
  details = signal<any | null>(null);
  stats = signal<any | null>(null);
  metrics = signal<any | null>(null);
  env = signal<any | null>(null);
  logs = signal<string[] | null>(null);
  threads = signal<any[] | null>(null);
  traces = signal<any[] | null>(null);

  ngOnInit() {
    this.refresh();
  }

  refresh() {
    this.dashboardService.getStats().subscribe(res => this.stats.set(res));
    this.dashboardService.getRuntime().subscribe(res => {
      const nodes: HoneycombNode[] = res.map((m: any) => ({
        id: m.mfeName,
        name: m.mfeName,
        status: m.status || 'REGISTERED',
        data: m
      }));
      this.mfeNodes.set(nodes);
    });
  }

  onNodeSelect(node: HoneycombNode) {
    this.selectedMfe.set(node);
    this.details.set(null);
    this.metrics.set(null);
    this.env.set(null);

    this.dashboardService.getMfeDetails(node.name).subscribe(res => this.details.set(res));
    this.dashboardService.getMfeMetrics(node.name).subscribe(res => this.metrics.set(res));
    this.dashboardService.getMfeEnv(node.name).subscribe(res => this.env.set(res));
    this.dashboardService.getMfeLogs(node.name).subscribe(res => this.logs.set(res));
    this.dashboardService.getMfeThreads(node.name).subscribe(res => this.threads.set(res));
    this.dashboardService.getMfeTraces(node.name).subscribe(res => this.traces.set(res));
  }
}
