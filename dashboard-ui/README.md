# Maverick Dashboard UI

A Federated Microfrontend Dashboard built with Angular, featuring advanced Runtime Discovery and Governance.

## Core Features
- **Runtime Discovery**: Dynamically resolves MFEs based on environment and user context.
- **Remote Lifecycle Hooks**: Protocol for initializing, mounting, and unmounting remote modules.
- **Dependency Graph**: Interactive visualization of the module ecosystem.

## Remote Lifecycle Hooks

We support a **Contract-Based Lifecycle Protocol** that allows remotes (Framework Agnostic) to control their lifecycle.
Remotes should export the following functions:

```typescript
export interface RemoteLifecycle {
    // Optional: Check readiness (e.g. feature flags, ref data)
    isReady?(): Promise<boolean>;

    // Required: Mount the UI into a native generic HTMLElement
    mount(container: HTMLElement, props: Record<string, any>): Promise<void>;

    // Required: Cleanup listeners and DOM
    unmount(container: HTMLElement): Promise<void>;

    // Optional: Global cleanup (stores, caches)
    dispose?(): Promise<void>;
}
```

### Implementing a Remote

You generally have **two options** when exposing a remote:

#### Option A: Standard Angular Component (Easiest)
Just expose your Standalone Component or NgModule. **No manual lifecycle hooks are required.** The `MfeHostComponent` handles the mounting and unmounting automatically.

**`webpack.config.js`**:
```javascript
exposes: {
    './ProfileWidget': './src/app/profile/profile.component.ts',
}
```

#### Option B: Advanced Lifecycle Protocol (For Control)
If you need to perform actions *before* rendering (e.g., checking permissions) or if you are using a different framework (React/Vue/Vanilla JS), implement the protocol manually in a new file (e.g., `remote-entry.ts`).

**1. Create the wrapper (`src/app/remote-entry.ts`)**:
```typescript
import { defineRemote } from '@maverick/runtime-discovery/remote-adapter'; // Functional Adapter
import { appConfig } from './app.config';
import { AppComponent } from './app.component';

export const { mount, unmount, isReady } = defineRemote({
    component: AppComponent,
    config: appConfig
});
```

**2. Update Webpack to expose the wrapper instead of the component**:

**`webpack.config.js`**:
```javascript
exposes: {
    // Point to the wrapper file that exports mount/unmount
    './ProfileWidget': './src/app/remote-entry.ts', 
}
```

### Usage with `MfeHostComponent` (Recommended)

The `MfeHostComponent` automatically detects if a loaded module adheres to this protocol.

```html
<mfe-host remoteName="remote-profile" exposedModule="ProfileWidget" [inputs]="{ userId: 123 }"></mfe-host>
```

- It calls `isReady()` before mounting.
- It passes a **Native Element** to `mount()`.
- It calls `unmount()` when the Angular component is destroyed.

### Manual Usage (Without MfeHostComponent)

If you need to load a remote manually (e.g., in a service, or a non-Angular part of the app), you can use `loadRemoteModule` and call the hooks directly.

```typescript
import { loadRemoteModule } from './core/runtime-discovery/remote-client';
import { RemoteLifecycle } from './core/runtime-discovery/lifecycle';

async function manualLoad(container: HTMLElement) {
    // 1. Load the module
    const module = await loadRemoteModule<RemoteLifecycle>('remote-profile', 'ProfileWidget', { type: 'module' });

    // 2. Check Readiness
    if (module.isReady) {
        const ready = await module.isReady();
        if (!ready) {
            console.warn('Remote not ready');
            return;
        }
    }

    // 3. Mount
    // Pass the native DOM element where you want it rendered
    await module.mount(container, { customProp: 'value' });

    // 4. Cleanup (Store module reference to call unmount later)
    return () => module.unmount(container);
}
```

## Development

Run `ng serve` for a dev server. Navigate to `http://localhost:4200/`.
