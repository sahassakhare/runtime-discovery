import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div style="border: 2px solid #ccc; padding: 20px; margin: 20px;">
      <h2>User Profile MFE</h2>
      <p>Version: <strong>{{ version }}</strong></p>
      
      <div *ngIf="newUiEnabled" style="background-color: #e0f7fa; padding: 10px;">
        <h3>✨ New Beta UI Enabled!</h3>
        <p>You are seeing the enhanced profile experience.</p>
      </div>

      <div *ngIf="!newUiEnabled">
        <p>Standard Profile View.</p>
      </div>
    </div>
  `
})
export class ProfileComponent implements OnInit {
  version = '1.0.0';
  newUiEnabled = false;

  ngOnInit() {
    // Check for globals injected by the Maverick Client
    const flags = (window as any).__MAVERICK_FLAGS__ || {};
    console.log('[Remote-Profile] Read Global Flags:', flags); // DEBUG LOG
    this.newUiEnabled = flags['profile.new-ui'] || false;
    console.log('[Remote-Profile] UI Enabled:', this.newUiEnabled); // DEBUG LOG

    // Check if we are the canary version based on some logic (or just hardcoded for demo)
    // In a real app, this value might be injected via build time replacement
    if (this.newUiEnabled) {
      this.version = '1.1.0-canary';
    }
  }
}
