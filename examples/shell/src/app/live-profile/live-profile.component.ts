import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MfeHostComponent } from '../../../../../src/runtime-discovery/mfe-host.component';

@Component({
  selector: 'app-live-profile',
  standalone: true,
  imports: [CommonModule, MfeHostComponent],
  template: `
    <div style="padding: 10px;">
      <!-- The Host Component manages the lifecycle with Governance reactivity -->
      <mfe-host 
        remoteName="remote-profile" 
        exposedModule="./ProfileComponent">
      </mfe-host>
    </div>
  `
})
export class LiveProfileComponent { }
