# @maverick/runtime-discovery

Maverick Runtime Discovery Client

## Features

- Dynamic Microfrontend Resolution via Discovery Server
- Sub-Resource Integrity (SRI) Support
- Resilient Loading with Fallback Mechanisms
- **Automatic Runtime Registration**: Applications self-register with the backend upon startup (url, appName, environment).
- **Runtime Version Presence**: Tracks active versions (primary vs fallback) loaded in the session.
- **Feature Flag Integration**: Built-in support for server-side feature toggles.
- Framework Agnostic Loader (optimized for Angular)

## Installation

```bash
npm install @maverick/runtime-discovery
```

## Backend Contract

Your discovery service should return a response adhering to the `ResolveRemoteResponse` interface.

**`POST https://api.example.com/resolve`**
**Body:** `{ "remoteName": "remote-profile", "environment": "production" }`

**Response:**
```json
{
  "remoteName": "remote-profile",
  "selected": {
    "version": "1.1.0-canary",
    "remoteEntry": "http://localhost:4201/remoteEntry.js",
    "integrity": "sha384-..."
  },
  "fallback": {
    "version": "1.0.0",
    "remoteEntry": "http://localhost:4201/remoteEntry.js",
    "integrity": "sha384-..."
  },
  "cacheTtlSeconds": 60,  
  "resolutionContext": {
    "flags": {
      "profile.new-ui": true,
      "global.dark-mode": true
    },
    "variant": {
      "name": "canary",
      "type": "canary"
    }
  }
}
```
```

## Runtime Registration

The library automatically calls the registration endpoint on startup. This allows the backend to build a live topology of running applications.

**`POST https://api.example.com/registry/instances`**
**Body:**
```json
{
  "appName": "remote-profile", 
  "environment": "production",
  "url": "https://profile.example.com" 
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
      environment: 'production',
      appName: 'shell-ui'
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

const discovery = new HttpRuntimeDiscovery('https://api.example.com', environment.name, 'my-app');
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
    path: 'profile',
    loadChildren: () => inject(REMOTE_CLIENT).loadRemoteModule('remote-profile', './ProfileModule')
  }
];
```

#### Using the Helper (Singleton)

If using the singleton config:

```typescript
import { remoteRoute } from '@maverick/runtime-discovery';
import { remoteClient } from './discovery.config';

export const routes: Routes = [
  // Dynamically loads 'remote-profile' from the URL returned by the discovery service
  remoteRoute(remoteClient, 'remote-profile', './ProfileModule')
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


## Feature Flags & Experimentation

The library integrates with a server-side feature service (Spring Boot + FF4J) to support feature toggles and canary releases.

### Usage in Components

Use the `FeatureClient` to fetch and check flags:

```typescript
import { FeatureClient } from '@maverick/runtime-discovery';

const featureClient = new FeatureClient('https://api.example.com');

// 1. Fetch flags for the current session context
await featureClient.fetchFlags({ userId: '123', role: 'beta-tester' });

// 2. Checking flags synchronously
if (featureClient.getFlag('profile.new-ui')) {
  // Show new UI
}
```

### Context-Aware Routing (Canary/Blue-Green)

You can pass a context object when resolving modules. This allows the discovery server to route users to specific variants (e.g., canary version).

```typescript
// Pass context during module load
await remoteClient.loadRemoteModule('remote-profile', './Module', 1, {
  userId: '123',
  region: 'us-east'
});
```

To enable this, your backend discovery service must support context evaluation.

### Integrated Flag Resolution

The `resolveRemote` response can now include feature flags directly, which can be integrated into your application state management (e.g., NgRx, Signals, or global window access) by the `RemoteClient`.

**Example Response**:
```json
{
  "remoteName": "remote-profile",
  "selected": { ... },
  "resolutionContext": {
    "flags": {
      "profile.new-ui": true,
      "profile.canary": { "strategy": "ponderation", "value": 0.1 }
    }
  }
}
```

This ensures that critical flags (like deployment variants) are available immediately upon module load, without waiting for a separate FeatureClient call.

### Configurable Fallback (Advanced)

By default, the client falls back to the `STABLE` release track if the selected version (e.g., Canary) fails to load. You can override this behavior per feature flag.

**UseCase**: You want to test a `CANARY` release but fallback to `BETA` instead of `STABLE` if it breaks.

**Configuration (FF4j Custom Properties)**:
1.  **`trackMapping`**: `CANARY` (Target Track)
2.  **`fallbackTrack`**: `BETA` (Fallback Track)

The resolver will prioritize `CANARY`. If `CANARY` is missing or the selection logic fails, it will attempt to return `BETA` as the fallback url.

## Deployment Strategies & Use Cases

The system supports multiple resolution strategies driven by FF4j algorithms.

### 1. Canary Releases (Traffic Splitting)
*   **Goal**: Gradually roll out a new version to a percentage of users to verify stability.
*   **Mechanism**: **Weighted Random Probability** (Stateless).
*   **Algorithm**: `DarkLaunchStrategy` (or `PonderationStrategy`).
    - Randomly selects a version based on configured weight (e.g., 20% Canary, 80% Stable).
    - **Use Case**: Testing infrastructure impact or general stability on a subset of traffic.

### 2. A/B Testing (Experimentation)
*   **Goal**: Compare user behavior between two variants (e.g., "New UI" vs "Old UI") with consistent user bucketing.
*   **Mechanism**: **Sticky Sessions / ID Hashing**.
*   **Algorithm**: `PonderationStrategy` (with Hashing) or `ExpressionFlipStrategy`.
    - Hashes a unique identifier (User ID, Session ID) to a bucket.
    - **Guarantees**: User X *always* sees Variant A; User Y *always* sees Variant B.
    - **Use Case**: UX conversion experiments, feature validation.

### 3. Normal Fallback (Resiliency)
*   **Goal**: Ensure high availability even if the cutting-edge version fails.
*   **Mechanism**: **Client-Side Failover**.
*   **Algorithm**: `Standard` (Default).
*   **Behavior**:
    - The client attempts to load the `Selected` version (Canary/Beta/Stable).
    - If loading fails (Network Error, 404, Script Error), it **automatically** loads the `Fallback` version (Stable).
    - **Note**: Fallback is now always provided, even if the primary version is Stable, to support retry mechanisms.

### 4. Handling Traffic Weights (Best Practice)
For advanced traffic splitting, use a standardized "Routing Flag" (e.g., `profile.routing`).
*   **Pattern**: Separation of concerns.
    *   `profile.routing`: Controls **Deployment** (Version Selection). Mapped to `CANARY`.
    *   `profile.new-ui`: Controls **Visibility** (Feature Toggle).
*   **Weight**: Managed via `trafficWeight` custom property (or FF4j Strategy) on the Routing Flag.
*   **Benefit**: You can deploy code (Canary) without showing the UI feature, enabling true "Dark Launches".

## Resilience & Fallback

The client includes a built-in failover mechanism:

1.  **Resolution**: Fetches configuration (`selected` and `fallback` versions).
2.  **Primary Attempt**: Tries to load the `selected` version.
3.  **Automatic Failover**: If the primary fails (e.g., 404, network error) and a `fallback` is provided in the discovery response, the client automatically attempts to load the fallback version.


## Failure & Rollback Playbooks

Operational procedures for handling common failure scenarios.

### 4.1 Discovery Server Down
*   **Behavior**: The Shell uses the last successful cached resolution (stored in `localStorage` or memory).
*   **Impact**: **No Total Outage**. The application continues to function using previously resolved module URLs (CDN).
*   **Recovery**: Once the server is back, the client automatically refreshes the configuration on the next session/reload.

### 4.2 Canary Failure (e.g., Error Rate Spike)
*   **Trigger**: Monitoring detects high error rate in the Canary version.
*   **Action**:
    1.  Go to FF4j Console.
    2.  Toggle `profile.routing` to **OFF** (or set `trackMapping` to `STABLE`).
*   **Result**:
    *   ✔ **Instant Rollback**: All new traffic immediately resolves to `STABLE`.
    *   ✔ **No Redeploy**: No code changes or CI/CD pipelines required.

### 4.3 Broken Version (Critical Defect)
*   **Scenario**: A specific version (e.g., `1.2.0`) has a critical bug but is not behind a feature flag.
*   **Action**:
    1.  Go to Dashboard / Database.
    2.  Set `active = false` for Version `1.2.0`.
*   **Result**: The Discovery Service will **never select** this version again. It will automatically find the next available active version (e.g., `1.1.0`).

### 4.4 Partial Region Failure
*   **Scenario**: One region (e.g., `us-east`) is degraded.
*   **Action**: Apply a policy override per environment in FF4j/Configuration.
*   **Result**: Redirect traffic to a healthy region's CDN or version without code changes.

## Development

```bash
# Build
npm run build

# Test
npm test

# Lint
npm run lint
```
