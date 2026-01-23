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

###  Governance & Policy Mesh (Dual-Mode Architecture)

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

###  Remote Lifecycle Protocol
To support advanced orchestration, the client enforces a strict lifecycle protocol for remotes. This allows the Shell to explicitly manage the **Initialization**, **Mounting**, and **Destruction** of microfrontends, preventing memory leaks and ensuring state consistency.

| Hook | Type | Description |
| :--- | :--- | :--- |
| **`isReady()`** | `Promise<boolean>` | **Pre-Flight Check**. Called before loading. Use this to validate Governance constraints, check Feature Flags, or ensure backend health. |
| **`mount(el, props)`** | `Promise<void>` | **Render**. The remote must render itself into the provided `HTMLElement` (`el`). It receives `props` (context/inputs) from the Host. |
| **`unmount(el)`** | `Promise<void>` | **Cleanup**. The remote must destroy its application instance and clean up DOM listeners. Critical for SPA performance. |
| **`dispose()`** | `Promise<void>` | **Global GC**. Optional hook to clear global caches or shared workers when the remote is evicted from the registry. |

---

## Installation

```bash
npm install @maverick/runtime-discovery
```

## Usage & Patterns

### 1. Configuration (Global)

Initialize the discovery strategy at the root of your application (e.g., `app.config.ts`). This sets the baseline identity and environment for all subsequent calls.

```typescript
import { provideDiscovery } from '@maverick/runtime-discovery';

export const appConfig: ApplicationConfig = {
  providers: [
    provideDiscovery({
      url: 'http://localhost:8081/api', // Discovery Service URL
      appName: 'shell',                 // MUST match the registered remote name
      environment: 'production',        // 'development' | 'staging' | 'production'
      tenantId: 'acme-corp'             // Optional: Multi-tenant context
    })
  ]
};
```

### 2. Route-Based Microfrontends (Robust Loading)

When loading a remote via Angular Router, it is **critical** to handle potential loading failures (e.g., network error, version incompatibility, or 404).

Use the following pattern to catch errors and re-throw them or redirect:

```typescript
import { loadRemoteModule } from '@maverick/runtime-discovery';

export const routes: Routes = [
    {
        path: 'profile',
        loadComponent: () =>
            loadRemoteModule('profile', './Profile', { type: 'module' })
                .then((m: any) => m.ProfileComponent)
                .catch(err => {
                    // CRITICAL: Log and handle the error
                    console.error('Failed to load profile remote:', err);
                    
                    // Option A: Re-throw to let Angular Router handle it (e.g., ErrorHandler)
                    throw err;

                    // Option B: Return a Fallback Component directly
                    // return import('./features/fallback/error.component').then(m => m.ErrorComponent);
                })
    }
];
```

### 3. Dynamic Widgets (Manual Loading)

For dashboard widgets or modal content where routing isn't applicable, use `loadRemoteModule` directly within a component.

```typescript
@Component({ ... })
export class DashboardComponent implements OnInit {
  container = viewChild('container', { read: ViewContainerRef });

  async loadWidget() {
    try {
      const module = await loadRemoteModule('remote-analytics', './WeeklyChart', { type: 'module' });
      this.container().createComponent(module.WeeklyChartComponent);
    } catch (e) {
      console.error('Widget unavailable', e);
      // Render skeleton or empty state
    }
  }
}
```

### 4. Handling Governance & Security Events

The library emits global window events when policy actions occur (e.g., a remote is blocked by OPA constraints or flagged for security).

```typescript
// Listen for Governance Alerts (e.g., Blocked by Policy)
window.addEventListener('maverick:governance_alert', (event: CustomEvent) => {
  const { remoteName, reason, timestamp } = event.detail;
  console.warn(`[Security] Access to ${remoteName} modified due to: ${reason}`);
  
  // Example: Show a toast notification to the user
  this.toastService.showWarning(`Policy Limited: ${reason}`);
});

// Listen for Feature Flag Updates (e.g., Real-time toggle)
window.addEventListener('maverick:flags_updated', (event: CustomEvent) => {
  const flags = event.detail;
  if (flags['global.maintenance-mode']) {
    this.router.navigate(['/maintenance']);
  }
});

// Listen for Variant Loading (e.g., A/B Test assignment)
window.addEventListener('maverick:variant_loaded', (event: CustomEvent) => {
   const { remoteName, resolutionContext } = event.detail;
   console.log(`Assigned to variant: ${resolutionContext.variant.name}`);
});
```

### 5. Multi-Version Testing (Context Propagation)

To force a specific variant (e.g., for testing a Canary release explicitly), you can pass a custom `context`. The backend uses this context to resolve the appropriate version.

```typescript
loadRemoteModule('profile', './Profile', {
  type: 'module',
  context: {
    // Explicitly request beta features if allowed by policy
    'user.role': 'beta-tester',
    'feature.new-ui': true 
  }
});
```

### 6. Framework-Agnostic Lifecycle Protocol

For non-Angular remotes (e.g., React/Vue wrapped as Web Components), the library supports a standardized lifecycle interface.

```typescript
// Remote Entry (exposed module)
export default {
    async mount(container: HTMLElement, props: any) {
        // Render React/Vue app
        ReactDOM.createRoot(container).render(<App {...props} />);
    },
    async unmount(container: HTMLElement) {
        // Cleanup
        ReactDOM.unmountComponentAtNode(container);
    }
}
```

Host Usage:
```typescript
const remote = await loadRemoteModule('react-widget', './Widget', { type: 'module' });
await remote.mount(document.getElementById('widget-container'), { user: '123' });
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

##  Governance Portal & Active Enforcement

The **Maverick Discovery Platform** includes a dedicated side-drawer dashboard for real-time governance:

1.  **Enforcement Grid**: View all active `REGO_OPA` and `Operational` policies.
2.  **Live Toggles**: Enable or disable any policy on-the-fly via the UI.
3.  **Fail-Open Mode**: If a policy is deactivated, the discovery service automatically skips evaluation for that protocol, ensuring high availability during maintenance.
4.  **Real-Time Metrics**: Monitor version skew, error rates, and governance compliance scores directly from the [Overview Dashboard](http://localhost:4203/overview).

---

##  Local Development & Orchestration

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
