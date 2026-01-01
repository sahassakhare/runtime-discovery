import { ApplicationConfig, importProvidersFrom, inject } from '@angular/core';
import { provideRouter, withRouterConfig } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { provideDiscovery } from '../../../../src/runtime-discovery/provide-discovery';
import { CONTEXT_PROVIDER } from '../../../../src/runtime-discovery/tokens';
import { AuthService } from './auth.service';

import { routes } from './app.routes';

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes, withRouterConfig({
      onSameUrlNavigation: 'reload'
    })),
    provideHttpClient(),
    provideDiscovery({
      url: 'http://localhost:8081/api', // Points to our Spring Boot Service
      environment: 'development',
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
