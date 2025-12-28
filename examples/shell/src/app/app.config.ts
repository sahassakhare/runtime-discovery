import { ApplicationConfig, importProvidersFrom } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { provideDiscovery } from '@maverick/runtime-discovery';

import { routes } from './app.routes';

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    provideHttpClient(),
    provideDiscovery({
      url: 'http://localhost:8081/api', // Points to our Spring Boot Service
      environment: 'development',
      appName: 'shell-ui'
    })
  ]
};
