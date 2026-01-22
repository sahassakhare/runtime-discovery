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
            <span class="mfe-name">{{node.name}}</span>
          </div>
        </div>
      </div>
    </div>
  `,
    styles: [`
    .honeycomb-container {
      display: flex;
      justify-content: center;
      padding: 40px;
      overflow: auto;
    }
    .honeycomb {
      display: flex;
      flex-wrap: wrap;
      width: 800px;
      transform: translateX(35px);
    }
    .hexagon {
      position: relative;
      width: 100px; 
      height: 57.74px;
      background-color: #444;
      margin: 30.87px 2px;
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      transition: all 0.3s ease;
      box-shadow: 0 0 10px rgba(0,0,0,0.3);
    }
    .hexagon:before,
    .hexagon:after {
      content: "";
      position: absolute;
      width: 0;
      border-left: 50px solid transparent;
      border-right: 50px solid transparent;
      left: 0;
    }
    .hexagon:before {
      bottom: 100%;
      border-bottom: 28.87px solid #444;
    }
    .hexagon:after {
      top: 100%;
      width: 0;
      border-top: 28.87px solid #444;
    }

    .hexagon:hover {
      transform: scale(1.1);
      z-index: 10;
    }

    /* Colors */
    .hexagon.healthy { background-color: #4caf50; }
    .hexagon.healthy:before { border-bottom-color: #4caf50; }
    .hexagon.healthy:after { border-top-color: #4caf50; }
    .hexagon.healthy { box-shadow: 0 0 15px rgba(76, 175, 80, 0.5); }
    
    .hexagon.unhealthy { background-color: #f44336; }
    .hexagon.unhealthy:before { border-bottom-color: #f44336; }
    .hexagon.unhealthy:after { border-top-color: #f44336; }

    .hexagon.live { background-color: #2196f3; }
    .hexagon.live:before { border-bottom-color: #2196f3; }
    .hexagon.live:after { border-top-color: #2196f3; }

    .hexagon.registered { background-color: #9e9e9e; }
    .hexagon.registered:before { border-bottom-color: #9e9e9e; }
    .hexagon.registered:after { border-top-color: #9e9e9e; }

    .hex-inner {
      color: white;
      text-align: center;
      font-size: 10px;
      font-weight: bold;
      z-index: 2;
      pointer-events: none;
      word-break: break-all;
      padding: 0 5px;
    }
  `]
})
export class HoneycombVisualizerComponent {
    @Input() nodes: HoneycombNode[] = [];
    @Output() nodeClick = new EventEmitter<HoneycombNode>();
}
