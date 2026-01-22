import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
    selector: 'app-settings',
    standalone: true,
    imports: [CommonModule, FormsModule],
    template: `
    <div class="card full-width animate-entry">
      <div class="header-row">
        <h2>Dashboard Settings</h2>
      </div>

      <div class="settings-form">
        <div class="setting-group">
          <label>Refresh Rate (seconds)</label>
          <input type="number" [(ngModel)]="refreshRate" class="input-field" min="5" max="300">
        </div>

        <div class="setting-group">
            <label class="toggle-label">
                <span>Dark Mode</span>
                <input type="checkbox" [(ngModel)]="darkMode" (change)="toggleTheme()">
            </label>
        </div>

        <div class="actions">
            <button class="btn primary" (click)="save()">Save Changes</button>
        </div>
      </div>
    </div>
  `,
    styles: [`
    .card {
        background: var(--bg-card);
        border: 1px solid var(--border-subtle);
        border-radius: var(--radius-lg);
        padding: 2rem;
        max-width: 600px;
    }
    .setting-group {
        margin-bottom: 2rem;
        display: flex;
        flex-direction: column;
        gap: 0.5rem;
    }
    label {
        color: var(--text-secondary);
        font-size: 0.9rem;
    }
    .input-field {
        background: rgba(0,0,0,0.2);
        border: 1px solid var(--border-subtle);
        color: var(--text-primary);
        padding: 0.75rem;
        border-radius: 6px;
        font-size: 1rem;
    }
    .btn {
        padding: 0.75rem 1.5rem;
        border-radius: 6px;
        border: none;
        cursor: pointer;
        font-weight: 600;
        font-size: 0.9rem;
    }
    .btn.primary {
        background: var(--primary);
        color: white;
    }
    .btn.primary:hover {
        background: #2563eb;
    }
  `]
})
export class SettingsComponent {
    refreshRate = 30;
    darkMode = true;

    toggleTheme() {
        // Mock theme toggle
        console.log('Theme toggled:', this.darkMode);
    }

    save() {
        alert('Settings saved!');
    }
}
