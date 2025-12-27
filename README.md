# @maverick/runtime-discovery

Maverick Runtime Discovery Client

## Features

- Dynamic Microfrontend Resolution via Discovery Server
- Sub-Resource Integrity (SRI) Support
- Resilient Loading with Fallback Mechanisms
- Framework Agnostic Loader (optimized for Angular)

## Installation

```bash
npm install @maverick/runtime-discovery
```

## Backend Contract

Your discovery service should return a response adhering to the `ResolveRemoteResponse` interface.

**`POST https://api.example.com/resolve`**
**Body:** `{ "remoteName": "payment-remote", "environment": "production" }`

**Response:**
```json
{
  "remoteName": "payment-remote",
  "selected": {
    "version": "1.2.0",
    "remoteEntry": "https://cdn.example.com/payment/1.2.0/remoteEntry.js",
    "integrity": "sha384-..."
  },
  "cacheTtlSeconds": 300,
  "fallback": {
    "version": "1.1.0",
    "remoteEntry": "https://cdn.example.com/payment/1.1.0/remoteEntry.js"
  }
}
```

## Usage

### 1. Configure the Client

#### Option 1: Angular Dependency Injection (Recommended)

This integrates with Angular's ecosystem, makes testing easier, and supports standard Angular `provide` patterns.

**In `src/app/app.config.ts`:**

```typescript
import { ApplicationConfig } from '@angular/core';
import { provideDiscovery } from '@maverick/runtime-discovery';

export const appConfig: ApplicationConfig = {
  providers: [
    provideDiscovery({
      url: 'https://api.example.com',
      environment: 'production'
    })
  ]
};
```

#### Option 2: Singleton (Simplest)

Useful for quick prototypes or when DI is not available.

**`src/app/discovery.config.ts`:**
```typescript
import { HttpRuntimeDiscovery, RemoteClient } from '@maverick/runtime-discovery';
import { environment } from '../environments/environment';

const discovery = new HttpRuntimeDiscovery('https://api.example.com', environment.name);
export const remoteClient = new RemoteClient(discovery);
```

### 2. Configuring Routes

#### Using Injection (Recommended)

If using DI config, use `inject` within a functional `loadChildren`:

```typescript
import { inject } from '@angular/core';
import { Routes } from '@angular/router';
import { REMOTE_CLIENT } from '@maverick/runtime-discovery';

export const routes: Routes = [
  {
    path: 'payment',
    loadChildren: () => inject(REMOTE_CLIENT).loadRemoteModule('payment-remote', './PaymentModule')
  }
];
```

#### Using the Helper (Singleton)

If using the singleton config:

```typescript
import { remoteRoute } from '@maverick/runtime-discovery';
import { remoteClient } from './discovery.config';

export const routes: Routes = [
  // Dynamically loads 'payment-remote' from the URL returned by the discovery service
  remoteRoute(remoteClient, 'payment-remote', './PaymentModule')
];
```

### 3. Manually Loading a Component

Sometimes you need to load a specific component dynamically (e.g., a widget) rather than a full route.

```typescript
import { Component, OnInit, inject } from '@angular/core';
import { REMOTE_CLIENT } from '@maverick/runtime-discovery';

@Component({ ... })
export class DashboardComponent implements OnInit {
  private remoteClient = inject(REMOTE_CLIENT);

  async ngOnInit() {
    try {
      const { WidgetComponent } = await this.remoteClient.loadRemoteModule<any>(
          'analytics-remote', 
          './WidgetComponent'
      );
      // Handle WidgetComponent...
    } catch (err) {
      console.error('Failed to load widget', err);
    }
  }
}
```

## Resilience & Fallback

The client includes a built-in failover mechanism:

1.  **Resolution**: Fetches configuration (`selected` and `fallback` versions).
2.  **Primary Attempt**: Tries to load the `selected` version.
3.  **Automatic Failover**: If the primary fails (e.g., 404, network error) and a `fallback` is provided in the discovery response, the client automatically attempts to load the fallback version.

## Development

```bash
# Build
npm run build

# Test
npm test

# Lint
npm run lint
```
