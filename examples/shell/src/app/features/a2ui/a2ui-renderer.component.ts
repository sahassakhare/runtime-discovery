import { Component, Input, OnInit, OnChanges, SimpleChanges, forwardRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Component as A2Component } from '../../core/services/orchestration-client.service';
import { MfeHostComponent } from '@maverick/runtime-discovery';

@Component({
  selector: 'a2ui-renderer',
  standalone: true,
  imports: [CommonModule, MfeHostComponent, forwardRef(() => A2uiRendererComponent)],
  template: `
    <!-- DEBUGGER OVERLAY (Hidden by default) -->
    <div class="debug-info text-[10px] text-gray-300 p-1 mb-1 font-mono hover:text-gray-500 transition-colors cursor-help" 
         *ngIf="false" title="Component ID: {{node.id}}">
        Running: {{ node.type }}
    </div>

    <ng-container [ngSwitch]="node.type">
      
      <!-- ============================================================================================== -->
      <!-- 📄 PAGE: The Root Canvas                                                                     -->
      <!-- ============================================================================================== -->
      <div *ngSwitchCase="'a2ui.v1.Page'" class="a2ui-page min-h-full bg-slate-50/50">
        <!-- Header -->
        <header *ngIf="node.title" class="mb-8 border-b border-slate-200 pb-4">
            <h1 class="text-3xl font-bold text-slate-900 tracking-tight">{{ node.title }}</h1>
            <p class="text-sm text-slate-500 mt-1">Orchestrated View • {{ node.id }}</p>
        </header>
        
        <!-- Content Flow -->
        <div class="a2ui-children grid grid-cols-1 gap-6 animate-in fade-in slide-in-from-bottom-4 duration-500">
            <ng-container *ngFor="let childId of node.children">
                <a2ui-renderer [node]="lookup(childId)" [componentMap]="componentMap" [dataModel]="dataModel"></a2ui-renderer>
            </ng-container>
        </div>
      </div>

      <!-- ============================================================================================== -->
      <!-- 📦 SECTION: Grouping & Layout                                                                -->
      <!-- ============================================================================================== -->
      <section *ngSwitchCase="'a2ui.v1.Section'" class="a2ui-section bg-white rounded-xl shadow-sm border border-slate-100 overflow-hidden"
               [class.col-span-full]="resolveValue('layout') === 'dashboard'">
        
        <!-- Optional Section Header -->
        <div *ngIf="node.title" class="bg-slate-50 px-6 py-4 border-b border-slate-100 flex items-center justify-between">
            <h3 class="font-semibold text-slate-800">{{ node.title }}</h3>
            <span class="text-xs font-medium text-slate-500 bg-white px-2 py-0.5 rounded-full ring-1 ring-slate-200 shadow-sm uppercase tracking-wide">
                {{ resolveValue('layout') || 'Section' }}
            </span>
        </div>
        
        <!-- Children Container with Adaptive Grid -->
        <div class="p-6 grid gap-6"
             [ngClass]="{
                'grid-cols-1': !resolveValue('layout') || resolveValue('layout') === 'stack',
                'grid-cols-1 md:grid-cols-2': resolveValue('layout') === 'grid',
                'grid-cols-1 md:grid-cols-3': resolveValue('layout') === 'dashboard',
                'grid-cols-1 border-l-4 border-indigo-500 pl-4': resolveValue('layout') === 'wizard'
             }">
            <ng-container *ngFor="let childId of node.children">
                <a2ui-renderer [node]="lookup(childId)" [componentMap]="componentMap" [dataModel]="dataModel"></a2ui-renderer>
            </ng-container>
        </div>
      </section>

      <!-- ============================================================================================== -->
      <!-- 📝 TEXT: Smart Content Display                                                               -->
      <!-- ============================================================================================== -->
      <div *ngSwitchCase="'a2ui.v1.Text'" class="a2ui-text">
        <p class="text-slate-600 leading-relaxed text-base max-w-prose">
            {{ resolveValue('text') }}
        </p>
      </div>

      <!-- ============================================================================================== -->
      <!-- 📊 TABLE: Data Grid                                                                          -->
      <!-- ============================================================================================== -->
      <div *ngSwitchCase="'a2ui.v1.Table'" class="a2ui-table overflow-hidden rounded-lg border border-slate-200 shadow-sm">
        <div class="overflow-x-auto">
            <table class="min-w-full divide-y divide-slate-200">
                <thead class="bg-slate-50">
                    <tr>
                        <th *ngFor="let col of resolveValue('columns') || []" 
                            class="px-6 py-3 text-left text-xs font-medium text-slate-500 uppercase tracking-wider">
                            {{ col }}
                        </th>
                    </tr>
                </thead>
                <tbody class="bg-white divide-y divide-slate-200">
                     <!-- Data Binding Handling: Try to resolve array, default to empty -->
                    <tr *ngFor="let row of getTableData()" class="hover:bg-slate-50/50 transition-colors">
                         <!-- Simple rendering assumption: row is object, col is key. Or if row is array? -->
                         <!-- Enterprise Rule: We assume row is object match cols -->
                        <td *ngFor="let col of resolveValue('columns') || []" class="px-6 py-4 whitespace-nowrap text-sm text-slate-700">
                            {{ row[col] || row[col.toLowerCase()] || '-' }}
                        </td>
                    </tr>
                </tbody>
            </table>
            
            <!-- Empty State -->
            <div *ngIf="!getTableData()?.length" class="p-8 text-center text-slate-400 text-sm">
                No data available for this view.
            </div>
        </div>
      </div>

      <!-- ============================================================================================== -->
      <!-- 🔘 BUTTON: Action Trigger                                                                    -->
      <!-- ============================================================================================== -->
      <div *ngSwitchCase="'a2ui.v1.Button'" class="a2ui-button inline-block">
        <button class="group relative inline-flex items-center justify-center px-5 py-2.5 text-sm font-medium text-white transition-all duration-200 bg-indigo-600 border border-transparent rounded-lg shadow-sm hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 active:scale-95 disabled:opacity-50 disabled:cursor-not-allowed">
             <!-- Icon Slot (Future) -->
             <span class="mr-2 opacity-0 -ml-4 group-hover:opacity-100 group-hover:ml-0 transition-all">👉</span>
             {{ resolveValue('label') || 'Action' }}
        </button>
      </div>

      <!-- ============================================================================================== -->
      <!-- 🧩 MFE HOST: The Crown Jewel                                                                 -->
      <!-- ============================================================================================== -->
      <div *ngSwitchCase="'a2ui.v1.Mfe'" class="a2ui-mfe group relative">
        <!-- Loading State / Error Boundary Wrapper -->
        <div class="absolute inset-0 bg-white/80 z-10 flex items-center justify-center backdrop-blur-sm transition-opacity opacity-0 pointer-events-none group-[.loading]:opacity-100">
            <div class="w-5 h-5 border-2 border-indigo-600 border-t-transparent rounded-full animate-spin"></div>
        </div>

        <div class="border border-slate-200 rounded-xl overflow-hidden shadow-sm bg-white transition-shadow hover:shadow-md">
             <!-- MFE Header (Optional, for context) -->
             <div class="bg-gradient-to-r from-slate-50 to-white px-4 py-2 border-b border-slate-100 flex justify-between items-center">
                 <div class="flex items-center gap-2">
                     <div class="w-2 h-2 rounded-full bg-green-500 animate-pulse"></div>
                     <span class="text-[10px] font-mono text-slate-400 uppercase tracking-wider">{{ node.remote }}</span>
                 </div>
                 <span class="text-[10px] text-slate-300">vLatest</span>
             </div>
             
             <!-- Remote Content -->
            <div class="p-0">
                <mfe-host 
                    [remoteName]="node.remote!" 
                    [exposedModule]="node.exposedModule!" 
                    [inputs]="node.props || {}">
                </mfe-host>
            </div>
        </div>
      </div>

      <!-- ============================================================================================== -->
      <!-- 🛑 ERROR: Unknown Component                                                                  -->
      <!-- ============================================================================================== -->
      <div *ngSwitchDefault class="p-4 bg-red-50 border border-red-200 rounded-lg text-red-600 text-sm flex items-center gap-3">
         <svg class="w-5 h-5 text-red-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"/>
         </svg>
         <div>
            <strong>Renderer Error:</strong> Unknown component type <code>{{ node.type }}</code>
         </div>
      </div>

    </ng-container>
  `
})
export class A2uiRendererComponent {
  @Input() node!: A2Component;
  @Input() componentMap!: Map<string, A2Component>;
  @Input() dataModel: Record<string, any> = {};

  lookup(id: string): A2Component {
    return this.componentMap.get(id)!;
  }

  resolveValue(key: string): any {
    const prop = this.node.props?.[key];
    const valuePath = this.node.props?.['valuePath'];

    // If it's a 'text' lookup and a valuePath exists, prioritize the path
    if (key === 'text' && valuePath && this.dataModel[valuePath]) {
      return this.dataModel[valuePath];
    }

    return prop;
  }

  getTableData(): any[] {
    // If explicit data array prop
    const explicit = this.node.props?.['data'];
    if (Array.isArray(explicit)) return explicit;

    // If reactive path
    const dataSourcePath = this.node.props?.['dataSource']; // e.g. "/employees"
    if (dataSourcePath && this.dataModel) {
      // Simple pointer logic: remove leading slash
      // TODO: proper JSON Pointer impl
      const key = dataSourcePath.replace(/^\//, '');
      // Check root
      if (Array.isArray(this.dataModel[dataSourcePath])) return this.dataModel[dataSourcePath];
      if (Array.isArray(this.dataModel[key])) return this.dataModel[key];

      // Check nested paths (simplified)
      // e.g. /profile/history -> dataModel['profile']['history']
      const parts = key.split('/');
      let current = this.dataModel;
      for (const part of parts) {
        if (current) current = current[part];
      }
      if (Array.isArray(current)) return current;
    }

    return [];
  }
}
