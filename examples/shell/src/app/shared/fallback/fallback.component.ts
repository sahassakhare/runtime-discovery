import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
    selector: 'app-mfe-fallback',
    standalone: true,
    imports: [CommonModule],
    template: `
    <div class="fallback-card">
      <div class="icon">⚠️</div>
      <div class="content">
        <h3>Service Temporarily Unavailable</h3>
        <p>We couldn't load the <strong>{{ remoteName }}</strong> module.</p>
        <button (click)="retry()">Retry</button>
      </div>
    </div>
  `,
    styles: []
})
export class FallbackComponent {
    @Input() remoteName: string = 'Requested';

    retry() {
        window.location.reload();
    }
}
