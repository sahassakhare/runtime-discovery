import { ApplicationConfig } from '@angular/core';
import { provideRouter } from '@angular/router';
import { routes } from './app.routes';
import { provideDiscovery } from '@maverick/runtime-discovery';

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    provideDiscovery({
      url: 'http://localhost:8081/api',
      environment: 'development',
      appName: 'remote-profile'
    })
  ]
};
