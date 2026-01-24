import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';

export interface HoneycombNode {
  id: string;
  name: string;
  status: 'REGISTERED' | 'HEALTHY' | 'UNHEALTHY' | 'LIVE' | 'None';
  data?: any;
}

@Component({
  selector: 'app-honeycomb-visualizer',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="honeycomb-container">
      <div class="honeycomb">
        <div *ngFor="let node of nodes" 
             class="hexagon" 
             [class]="node.status.toLowerCase()"
             (click)="nodeClick.emit(node)">
          <div class="hex-inner">
            <div class="mfe-name">{{node.name}}</div>
            <div class="mfe-version" *ngIf="node.data?.activeVersion || node.data?.version">{{node.data?.activeVersion || node.data?.version || 'v?'}}</div>
            <div class="mfe-skew" *ngIf="node.data?.versionSkew && node.data?.versionSkew !== 'Aligned'">{{node.data?.versionSkew}}</div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .honeycomb-container {
      display: flex;
      justify-content: center;
      padding: 60px 20px;
      overflow-y: auto;
      overflow-x: hidden;
      width: 100%;
    }
    .honeycomb {
      display: flex;
      flex-wrap: wrap;
      justify-content: center;
      width: 90%;
      max-width: 1200px;
    }
    
    /* 
      Hexagon CSS - Scaled up for "Wallboard" feel
      Base Width: 180px
      Height: 180 * tan(30deg) = 180 * 0.577 = 103.92px
      Side/Pyramid Height: 103.92 / 2 = 51.96px
    */
    .hexagon {
      position: relative;
      width: 180px; 
      height: 104px;
      background-color: #555;
      margin: 54px 4px; /* V-margin = Side Height + spacing */
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      transition: all 0.2s ease-in-out;
      box-shadow: 0 4px 12px rgba(0,0,0,0.4);
      flex-shrink: 0;
    }
    
    .hexagon:before,
    .hexagon:after {
      content: "";
      position: absolute;
      width: 0;
      border-left: 90px solid transparent; /* Width / 2 */
      border-right: 90px solid transparent;
      left: 0;
      transition: border-color 0.2s ease-in-out;
    }
    
    .hexagon:before {
      bottom: 100%;
      border-bottom: 52px solid #555; /* Side Height */
    }
    .hexagon:after {
      top: 100%;
      width: 0;
      border-top: 52px solid #555;
    }

    .hexagon:hover {
      transform: scale(1.05) translateY(-5px);
      z-index: 10;
      filter: brightness(1.1);
    }

    /* SBA Inspired Colors */
    .hexagon.healthy { background-color: #2ecc71; } /* Emerald */
    .hexagon.healthy:before { border-bottom-color: #2ecc71; }
    .hexagon.healthy:after { border-top-color: #2ecc71; }
    
    .hexagon.unhealthy { background-color: #e74c3c; } /* Alizarin */
    .hexagon.unhealthy:before { border-bottom-color: #e74c3c; }
    .hexagon.unhealthy:after { border-top-color: #e74c3c; }

    .hexagon.live { background-color: #3498db; } /* Peter River */
    .hexagon.live:before { border-bottom-color: #3498db; }
    .hexagon.live:after { border-top-color: #3498db; }

    .hexagon.registered { background-color: #7f8c8d; } /* Concrete */
    .hexagon.registered:before { border-bottom-color: #7f8c8d; }
    .hexagon.registered:after { border-top-color: #7f8c8d; }
    
    /* Down/Offline */
    .hexagon.down { background-color: #c0392b; }
    .hexagon.down:before { border-bottom-color: #c0392b; }
    .hexagon.down:after { border-top-color: #c0392b; }

    .hex-inner {
      color: white;
      text-align: center;
      z-index: 2;
      pointer-events: none;
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 4px;
    }
    
    .mfe-name {
      font-size: 16px;
      font-weight: 800;
      text-transform: uppercase;
      letter-spacing: 0.5px;
      text-shadow: 0 1px 2px rgba(0,0,0,0.3);
      max-width: 140px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
    
    .mfe-version {
      font-size: 12px;
      opacity: 0.9;
      font-family: 'JetBrains Mono', monospace;
      background: rgba(0,0,0,0.15);
      padding: 2px 8px;
      border-radius: 10px;
    }
    
    .mfe-skew {
      font-size: 10px;
      color: #fff;
      background: #d35400;
      padding: 1px 6px;
      border-radius: 4px;
      margin-top: 2px;
    }
  `]
})
export class HoneycombVisualizerComponent {
  @Input() nodes: HoneycombNode[] = [];
  @Output() nodeClick = new EventEmitter<HoneycombNode>();
}
