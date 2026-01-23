import { Component, Input, OnInit, OnChanges, SimpleChanges, forwardRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Component as A2Component } from '../../core/services/orchestration-client.service';
import { MfeHostComponent } from '@maverick/runtime-discovery';

@Component({
  selector: 'a2ui-renderer',
  standalone: true,
  imports: [CommonModule, MfeHostComponent, forwardRef(() => A2uiRendererComponent)],
  template: `
    <div class="debug-info text-xs text-gray-400 p-1 border border-dashed mb-2" *ngIf="false">
        DEBUG: {{ node.type }}
    </div>
    <ng-container [ngSwitch]="node.type">
      
      <!-- PAGE CONTAINER -->
      <div *ngSwitchCase="'a2ui.v1.Page'" class="a2ui-page p-6">
        <h1 *ngIf="node.title" class="text-3xl font-bold mb-4">{{ node.title }}</h1>
        <div class="a2ui-children space-y-4">
            <ng-container *ngFor="let childId of node.children">
                <a2ui-renderer [node]="lookup(childId)" [componentMap]="componentMap"></a2ui-renderer>
            </ng-container>
        </div>
      </div>

      <!-- TEXT COMPONENT -->
      <p *ngSwitchCase="'a2ui.v1.Text'" class="text-gray-700">
        {{ node.props?.['text'] }}
      </p>

      <!-- BUTTON COMPONENT -->
      <button *ngSwitchCase="'a2ui.v1.Button'" class="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700">
        {{ node.props?.['label'] || 'Action' }}
      </button>

      <!-- MFE CONTAINER -->
      <div *ngSwitchCase="'a2ui.v1.Mfe'" class="border border-gray-200 rounded p-4">
        <mfe-host 
            [remoteName]="node.remote!" 
            [exposedModule]="node.exposedModule!" 
            [inputs]="node.props || {}">
        </mfe-host>
      </div>

      <!-- FALLBACK -->
      <div *ngSwitchDefault class="text-red-500">
        Unknown component: {{ node.type }}
      </div>

    </ng-container>
  `
})
export class A2uiRendererComponent {
  @Input() node!: A2Component;
  @Input() componentMap!: Map<string, A2Component>;

  lookup(id: string): A2Component {
    return this.componentMap.get(id)!;
  }
}
