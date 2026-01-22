import { ApplicationConfig, importProvidersFrom, inject } from '@angular/core';
import { provideRouter, withRouterConfig } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { provideAnimations } from '@angular/platform-browser/animations';
import { provideDiscovery } from './core/runtime-discovery/provide-discovery';
import { CONTEXT_PROVIDER } from './core/runtime-discovery/tokens';
import { AuthService } from './auth.service';

import { routes } from './app.routes';

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes, withRouterConfig({
      onSameUrlNavigation: 'reload'
    })),
    provideHttpClient(),
    provideAnimations(),
    provideDiscovery({
      url: 'http://localhost:8081/api', // Points to our Spring Boot Service
      environment: 'production',
      appName: 'shell-ui'
    }),
    {
      provide: CONTEXT_PROVIDER,
      useFactory: () => {
        const auth = inject(AuthService);
        return () => auth.getContext();
      }
    }
  ]
};
