import { Component, Input, OnInit, OnChanges, ViewContainerRef, ViewChild, OnDestroy, SimpleChanges, effect, Injector, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LiveDiscoveryService } from './live-discovery.service';
import { REMOTE_CLIENT, CONTEXT_PROVIDER } from './tokens';
import { loadRemoteModule } from './remote-client';
import { RemoteLifecycle } from './lifecycle';

@Component({
    selector: 'mfe-host',
    standalone: true,
    imports: [CommonModule],
    template: `<ng-template #container></ng-template>`
})
export class MfeHostComponent implements OnInit, OnChanges, OnDestroy {
    @Input() remoteName!: string;
    @Input() exposedModule!: string;
    @Input() inputs: Record<string, any> = {};

    @ViewChild('container', { read: ViewContainerRef, static: true })
    container!: ViewContainerRef;

    private currentVersion: string | null = null;
    private currentConfig: any | null = null;
    private loadedModule: RemoteLifecycle | null = null; // Track lifecycle module

    private liveDiscovery = inject(LiveDiscoveryService);
    private contextProvider = inject(CONTEXT_PROVIDER, { optional: true });
    private injector = inject(Injector);

    constructor() {
        // React to configuration changes is handled in ngOnInit effect
    }

    ngOnInit() {
        this.liveDiscovery.monitor(this.remoteName);

        // Initial Load
        this.loadMfe();

        // Setup Effect explicitly to watch for updates
        const configSignal = this.liveDiscovery.getRemoteConfig(this.remoteName);

        effect(() => {
            const config = configSignal();
            if (config) {
                console.log(`[MfeHost] Effect triggered for ${this.remoteName}. New Version: ${config.selected.version}, Current: ${this.currentVersion}`);
                // Check if Version OR Flags changed
                const currentFlags = JSON.stringify(this.currentConfig?.resolutionContext?.flags || {});
                const newFlags = JSON.stringify(config.resolutionContext?.flags || {});

                if (config.selected.version !== this.currentVersion || currentFlags !== newFlags) {
                    console.log(`[MfeHost] Change detected for ${this.remoteName}. Reloading...`);
                    this.currentConfig = config;
                    this.loadMfe();
                } else {
                    console.log(`[MfeHost] Config matched. No reload needed.`);
                }
            }
        }, { injector: this.injector });
    }

    ngOnChanges(changes: SimpleChanges) {
        if (changes['remoteName'] && !changes['remoteName'].firstChange) {
            this.liveDiscovery.monitor(this.remoteName);
            this.loadMfe();
        }
    }

    async loadMfe() {
        // 1. Cleanup previous
        this.cleanup();
        this.container.clear();

        try {
            const module = await loadRemoteModule<any>(
                this.remoteName,
                this.exposedModule,
                {
                    type: 'module',
                    retries: 1,
                    context: this.contextProvider ? this.contextProvider() : {}
                }
            );

            // 2. Protocol Check: RemoteLifecycle
            if (this.isLifecycleModule(module)) {
                await this.mountLifecycleModule(module);
            }
            // 3. Fallback: Legacy Angular Component
            else {
                this.mountLegacyComponent(module);
            }

            // Update version tracking
            const config = this.liveDiscovery.getRemoteConfig(this.remoteName)();
            if (config) {
                this.currentVersion = config.selected.version;
            }

        } catch (err) {
            console.error(`[MfeHost] Failed to load ${this.remoteName}`, err);
        }
    }

    private isLifecycleModule(module: any): module is RemoteLifecycle {
        return typeof module.mount === 'function' && typeof module.unmount === 'function';
    }

    private async mountLifecycleModule(module: RemoteLifecycle) {
        this.loadedModule = module;
        const nativeElement = this.container.element.nativeElement.parentElement; // Get parent of comment node

        // Optional: isReady check
        if (module.isReady) {
            const ready = await module.isReady();
            if (!ready) {
                console.warn(`[MfeHost] Remote ${this.remoteName} reported not ready.`);
                return;
            }
        }

        // Mount
        await module.mount(nativeElement, this.inputs);
        console.log(`[MfeHost] Mounted lifecycle module: ${this.remoteName}`);
    }

    private mountLegacyComponent(module: any) {
        const ComponentType = module.default || Object.values(module)[0];

        if (ComponentType) {
            const ref = this.container.createComponent(ComponentType);
            if (this.inputs) {
                Object.assign(ref.instance as object, this.inputs);
            }
        } else {
            console.error(`[MfeHost] No component found in exposed module ${this.exposedModule}`);
        }
    }

    private cleanup() {
        if (this.loadedModule) {
            // Unmount lifecycle module
            const nativeElement = this.container.element.nativeElement.parentElement;
            this.loadedModule.unmount(nativeElement).catch(e => console.error(`[MfeHost] Error unmounting ${this.remoteName}`, e));

            if (this.loadedModule.dispose) {
                this.loadedModule.dispose().catch(e => console.error(`[MfeHost] Error disposing ${this.remoteName}`, e));
            }
            this.loadedModule = null;
        }
        // container.clear() handles legacy component destruction automatically via Angular
    }

    ngOnDestroy() {
        this.cleanup();
    }
}
