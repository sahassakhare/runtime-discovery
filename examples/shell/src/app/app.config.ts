import { ApplicationConfig, importProvidersFrom, inject } from '@angular/core';
import { provideRouter, withRouterConfig } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { provideAnimations } from '@angular/platform-browser/animations';
import { provideDiscovery, CONTEXT_PROVIDER } from '@maverick/runtime-discovery';
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
      appName: 'shell'
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
