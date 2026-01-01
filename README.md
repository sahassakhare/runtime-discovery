# @maverick/runtime-discovery

**Enterprise-Grade Microfrontend Discovery Client**

A robust, framework-agnostic client for resolving, loading, and hot-swapping microfrontends at runtime. Designed for scale, this library supports multi-tenancy, environment isolation, and real-time updates via Server-Sent Events (SSE).

---

##  Key Features

###  Enterprise Multi-Tenancy
*   **Tenant Isolation**: delivering specific versions of microfrontends based on the active `tenantId`.
*   **Context-Aware Resolution**: Different tenants can see different versions (e.g., "Premium Users" get v2.0, "Standard" get v1.0).

###  Live Hot Swapping (Fan-Out Architecture)
*   **Real-Time Updates**: Instantly push updates to connected clients using **Server-Sent Events (SSE)**.
*   **Granular Swapping**: Uses a "Push-Signal, Pull-Data" model. The server sends a lightweight `CONFIG_CHANGED` signal; the client intelligently diffs Version and Feature Flags to reload *only* the affected MFEs.
*   **Flicker-Free**: Components remain stable unless a specific update targeting them is detected.

###  Environment Scoping
*   **Environment Isolation**: Distinct deployments for `development`, `staging`, and `production`.
*   **Safe Promotion**: Promote immutable `Version` artifacts across environments without rebuilding.

###  Feature Flags & Experimentation
*   **Deep Integration**: Native support for **FF4j** and **OpenFeature**.
*   **Canary Releases**: Route traffic to new versions based on percentage weights or user context.
*   **Dark Launches**: Deploy code silently and toggle visibility via feature flags (`profile.new-ui`).

###  Governance & Policy Mesh (Dual-Mode Architecture) 🛡️

The platform features a sophisticated **Hybrid Policy Engine** that can operate in two distinct modes, configurable via `application.yml`.

#### 1. Architecture: Database + Classpath Hybrid
*   **Database (The Catalog)**: Stores policy metadata (ID, Type, Active Status). Acts as the global "On/Off" switch.
*   **Classpath (The Logic)**: Stores immutable, pre-compiled WASM binaries (`policies/POL-MFE-XX.wasm`) inside the application JAR.

This ensures **Production Stability** (logic is versioned with the release) while maintaining **Runtime Agility** (instant toggle of active policies).

#### 2. Modes of Operation
| Mode | Description | Use Case |
| :--- | :--- | :--- |
| **Embedded (Recommended)** | Runs policies in-process using ASM/WASM. Zero network latency, no external binary required. | Production, High-Performance scaling. |
| **Sidecar** | Offloads evaluation to a local OPA server (`:8181`). Allows hot-reloading of `.rego` files without builds. | Local Development, Policy debugging. |

#### 3. Policy Context Integration (The Payload)
To enforce policies effectively, the client sends a rich `context` object in the payload. This maps directly to `input` variables in Rego policies.

**Client Request (`api/resolve`):**
```json
{
  "remoteName": "remote-profile",
  "appName": "shell",
  "context": {
    "user.authenticated": true,
    "user.roles": ["ADMIN", "USER"],
    "user.id": "internal-user",
    "user.internal": true,
    "user.department": "FINANCE",
    "feature.new-ui": true
  }
}
```

**Rego Policy Example (`policies/mfe/access.rego`):**
```rego
package mfe.access

default allow = false

# Rule: Allow if user is authenticated
allow {
  input.user.authenticated == true
}

# Rule: Allow specific department for sensitive MFEs
allow {
  input.mfeName == "remote-audit"
  input.user.department == "FINANCE"
}
```

#### 4. Policy Development Workflow

**A. Developing Policies**
1.  Edit `.rego` files in `mfe-feature-service/policies/mfe/`.
2.  (Optional) Run Sidecar mode for instant feedback.

**B. Compiling for Production (Embedded Mode)**
The Embedded mode requires `.wasm` binaries. Use the provided cross-platform scripts to compile them into `src/main/resources/policies`.

*   **macOS/Linux**: `./compile_policies.sh`
*   **Windows**: `compile_policies.bat`

This process:
1.  Downloads OPA binary (if missing).
2.  Compiles individual policies (access, discovery, etc.).
3.  Bundles ALL policies into the unified decision policy (`POL-MFE-09`).
4.  Places artifacts in the classpath resource folder (`src/main/resources/policies`) for packaging.

---

###  Resilience & Fallback
*   **Automatic Failover**: If a primary version (e.g., Canary) fails to load (404/Network Error), the client automatically falls back to a stable version.
*   **Discovery Caching**: Caches resolution responses to survive temporary Discovery Server outages.

###  Automatic Runtime Registration
*   **Self-Discovery**: Applications automatically register themselves with the backend upon startup, building a live topology of the system.

---

## Installation

```bash
npm install @maverick/runtime-discovery
```

## Usage

### 1. Configuration (Angular)

Integrate with Angular's dependency injection system using `provideDiscovery`.

**`src/app/app.config.ts`**:
```typescript
import { ApplicationConfig } from '@angular/core';
import { provideDiscovery } from '@maverick/runtime-discovery';

export const appConfig: ApplicationConfig = {
  providers: [
    provideDiscovery({
      url: 'https://api.example.com',
      appName: 'shell-ui',
      environment: 'production', // 'development' | 'staging' | 'production'
      tenantId: 'acme-corp'      // Optional: Multi-tenant context
    })
  ]
};
```

### 2. Live Discovery Service (Hot Swapping)

The `LiveDiscoveryService` expects an SSE endpoint at `/api/stream`. It automatically connects and manages the lifecycle of all microfrontends on the page.

**No additional code required**—simply using the `MfeHostComponent` or `RemoteClient` registers the remote for updates.

### 3. Loading Microfrontends

#### Option A: Routing (Lazy Loading)

Load a full microfrontend module when a route is activated.

```typescript
import { inject } from '@angular/core';
import { Routes } from '@angular/router';
import { REMOTE_CLIENT } from '@maverick/runtime-discovery';

export const routes: Routes = [
  {
    path: 'profile',
    // Dynamically resolves metadata, then loads the bundle
    loadChildren: () => inject(REMOTE_CLIENT).loadRemoteModule('remote-profile', './ProfileModule')
  }
];
```

#### Option B: Component (Dynamic Widget)

Embed a microfrontend anywhere in your template using the Host Component.

```html
<!-- src/app/dashboard.component.html -->
<mfe-host 
    remoteName="remote-profile" 
    exposedModule="./UserProfile" 
    [inputs]="{ userId: 123 }">
</mfe-host>
```

---

## Backend Contract

The discovery service must expose the following endpoints:

### 1. Resolution Endpoint
**`POST /api/resolve`**

**Request:**
```json
{
  "remoteName": "remote-profile",
  "environment": "production",
  "tenantId": "acme-corp",
  "context": { "userRole": "beta" }
}
```

**Response:**
```json
{
  "remoteName": "remote-profile",
  "selected": {
    "version": "1.2.0-rc1",
    "remoteEntry": "https://cdn.example.com/mfe/v1.2.0/remoteEntry.js",
    "integrity": "sha384-..."
  },
  "fallback": {
    "version": "1.0.0",
    "remoteEntry": "https://cdn.example.com/mfe/v1.0.0/remoteEntry.js"
  },
  "resolutionContext": {
    "flags": {
      "profile.new-ui": true,
      "global.dark-mode": false
    },
    "variant": {
      "name": "canary",
      "type": "canary"
    }
  }
}
```

### 2. SSE Stream (Hot Swapping)
**`GET /api/stream?appName=shell&env=production&tenantId=acme`**

*   **Event**: `message`
*   **Data**: `CONFIG_CHANGED`

---

## Deployment Strategies

The client supports sophisticated deployment strategies driven by the backend:

| Strategy | Description | Mechanism |
| :--- | :--- | :--- |
| **Standard** | Direct mapping to the Active version. | Default behavior. |
| **Canary** | Traffic splitting (e.g., 10% users). | Weighted probability via Feature Flags. |
| **A/B Test** | Persistent user bucketing. | Sticky sessions / Hashing. |
| **Blue/Green** | Instant environment switch. | Toggling the active Deployment version. |
| **Tenant Specific** | Custom version for a specific tenant. | Tenant ID match in `Deployment` table. |

---

##  Governance Portal & Active Enforcement 🛡️

The **Maverick Discovery Platform** includes a dedicated side-drawer dashboard for real-time governance:

1.  **Enforcement Grid**: View all active `REGO_OPA` and `Operational` policies.
2.  **Live Toggles**: Enable or disable any policy on-the-fly via the UI.
3.  **Fail-Open Mode**: If a policy is deactivated, the discovery service automatically skips evaluation for that protocol, ensuring high availability during maintenance.
4.  **Real-Time Metrics**: Monitor version skew, error rates, and governance compliance scores directly from the [Overview Dashboard](http://localhost:4203/overview).

---

##  Local Development & Orchestration 🌐

To run the full ecosystem locally, ensure the following ports are available:

| Port | Component | Role |
| :--- | :--- | :--- |
| **4200** | [MFE Shell](http://localhost:4200) | Primary MFE Container. |
| **4201** | [Remote Profile](http://localhost:4201) | Mock Microfrontend (Target). |
| **4203** | [Dashboard UI](http://localhost:4203) | Management & Governance Portal. |
| **8081** | [Discovery Server](http://localhost:8081) | Java/Spring Boot API. |
| **8181** | [OPA Server](http://localhost:8181) | Rego Policy Engine (Sidecar Mode). |

---

## Development

```bash
# Build the core library
npm run build

# Start the full ecosystem (Root)
npm start
```
