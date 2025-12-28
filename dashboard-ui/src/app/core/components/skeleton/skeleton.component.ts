import { Component, Input } from '@angular/core';

@Component({
    selector: 'app-skeleton',
    standalone: true,
    template: `
    <div class="skeleton" 
         [style.width]="width" 
         [style.height]="height" 
         [style.border-radius]="borderRadius">
    </div>
  `,
    styles: [`
    .skeleton {
      background: linear-gradient(90deg, rgba(255,255,255,0.05) 25%, rgba(255,255,255,0.1) 50%, rgba(255,255,255,0.05) 75%);
      background-size: 200% 100%;
      animation: shimmer 1.5s infinite;
      display: inline-block;
    }

    @keyframes shimmer {
      0% { background-position: 200% 0; }
      100% { background-position: -200% 0; }
    }
  `]
})
export class SkeletonComponent {
    @Input() width = '100%';
    @Input() height = '1em';
    @Input() borderRadius = '4px';
}
