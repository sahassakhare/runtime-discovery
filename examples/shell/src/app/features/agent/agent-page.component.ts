import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { OrchestrationClientService, Component as A2Component, UiResponse } from '../../core/services/orchestration-client.service';
import { A2uiRendererComponent } from '../a2ui/a2ui-renderer.component';

@Component({
    selector: 'app-agent-page',
    standalone: true,
    imports: [CommonModule, FormsModule, A2uiRendererComponent],
    template: `
    <div class="container mx-auto p-8">
      <h1 class="text-4xl font-bold mb-8">Agent Orchestrator</h1>
      
      <div class="flex gap-4 mb-8">
        <input 
            type="text" 
            [(ngModel)]="intent" 
            placeholder="Describe your intent (e.g. 'View my tax forms')"
            class="flex-1 p-3 border rounded shadow-sm"
            (keyup.enter)="submit()"
        >
        <button 
            (click)="submit()"
            [disabled]="loading"
            class="bg-black text-white px-6 py-3 rounded font-medium disabled:opacity-50">
            {{ loading ? 'Thinking...' : 'Orchestrate' }}
        </button>
      </div>

      <div *ngIf="error" class="bg-red-100 text-red-700 p-4 rounded mb-8">
        {{ error }}
      </div>

      <div *ngIf="response" class="bg-white border rounded shadow-lg p-8 min-h-[400px]">
        <a2ui-renderer 
            [node]="rootNode!" 
            [componentMap]="componentMap"
            [dataModel]="dataModel">
        </a2ui-renderer>
      </div>
    </div>
  `
})
export class AgentPageComponent {
    intent = '';
    loading = false;
    error: string | null = null;

    response: UiResponse | null = null;
    rootNode: A2Component | undefined;
    componentMap = new Map<string, A2Component>();
    dataModel: Record<string, any> = {};

    constructor(private orchestration: OrchestrationClientService) { }

    submit() {
        if (!this.intent.trim()) return;

        this.loading = true;
        this.error = null;
        this.response = null;
        this.componentMap.clear();

        this.orchestration.orchestrate(this.intent).subscribe({
            next: (res) => {
                this.response = res;
                this.processResponse(res);
                this.loading = false;
            },
            error: (err) => {
                console.error(err);
                this.error = 'Failed to orchestrate intent. Check the backend.';
                this.loading = false;
            }
        });
    }

    private processResponse(res: UiResponse) {
        // Clear state
        this.componentMap.clear();
        this.dataModel = {};

        // Build adjacency map
        res.surfaceUpdate.components.forEach(c => {
            this.componentMap.set(c.id, c);
        });

        // Initialize Data Model
        if (res.dataModelUpdate?.paths) {
            this.dataModel = { ...res.dataModelUpdate.paths };
        }

        // Find root
        const rootId = res.surfaceUpdate.root;
        this.rootNode = this.componentMap.get(rootId);
    }
}
