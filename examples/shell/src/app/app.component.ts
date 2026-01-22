import { Component, OnInit, Injector, inject, signal, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, RouterLink, Router, NavigationEnd } from '@angular/router';
import { setAppInjector } from './app-injector';
import { AuthService } from './auth.service';
import { LiveDiscoveryService } from './core/runtime-discovery/live-discovery.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent implements OnInit {
  title = 'shell';
  flags = {};
  isEnforcementEnabled = signal<boolean | null>(null);
  isLiveRoute = signal<boolean>(false);

  private auth = inject(AuthService);
  private liveDiscovery = inject(LiveDiscoveryService);
  private router = inject(Router);
  private cdr = inject(ChangeDetectorRef);
  isLoggedIn = this.auth.isLoggedIn;

  constructor(private injector: Injector) {
    setAppInjector(this.injector);
  }

  ngOnInit() {
    // Listen for flag updates from the library
    window.addEventListener('maverick:flags_updated', (e: any) => {
      this.flags = e.detail;
      console.log('Shell received flags:', this.flags);
      this.cdr.detectChanges(); // Respecting user's suggestion to use detectChanges if needed for UI
    });

    // Track active route for UI visibility
    this.router.events.subscribe(event => {
      if (event instanceof NavigationEnd) {
        const isLive = event.url.includes('/live');
        this.isLiveRoute.set(isLive);

        if (isLive) {
          this.liveDiscovery.connect();
        } else {
          this.liveDiscovery.disconnect();
        }

        this.cdr.detectChanges();
      }
    });

    this.checkEnforcement();
  }

  toggleLogin() {
    if (this.isLoggedIn()) {
      this.auth.logout();
    } else {
      this.auth.login();
    }

    // 1. Instantly refresh monitored backgrounds
    this.liveDiscovery.refreshAll();

    // 2. Perform a Router Context Refresh (Satisfying "standard routing" requirement)
    // We navigate to essentially "reload" the current component with fresh context
    const currentUrl = this.router.url;
    this.router.navigateByUrl('/', { skipLocationChange: true }).then(() => {
      this.router.navigate([currentUrl]);
    });

    // 3. Perform manual change detection as requested by user
    this.cdr.detectChanges();
  }

  async checkEnforcement() {
    try {
      const res = await fetch('http://localhost:8081/api/features/check/governance.enforcement');
      const status = await res.json();
      this.isEnforcementEnabled.set(status);
      this.cdr.detectChanges();
    } catch (e) {
      console.warn('Failed to fetch enforcement status');
    }
  }
}
