import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MfeHostComponent } from '../../../../../src/runtime-discovery/mfe-host.component';

@Component({
    selector: 'app-live-profile',
    standalone: true,
    imports: [CommonModule, MfeHostComponent],
    template: `
    <div style="border: 2px dashed #e74c3c; padding: 20px; border-radius: 8px;">
      <h2>Live Hot-Swap Demo</h2>
      <p>This component monitors the backend for version changes every 10 seconds.</p>
      
      <!-- The Host Component manages the lifecycle -->
      <mfe-host 
        remoteName="remote-profile" 
        exposedModule="./ProfileComponent">
      </mfe-host>
    </div>
  `
})
export class LiveProfileComponent { }
