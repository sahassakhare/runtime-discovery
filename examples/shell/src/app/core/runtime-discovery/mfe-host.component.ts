import { Component, Input, OnInit, OnChanges, ViewContainerRef, ViewChild, OnDestroy, SimpleChanges, effect, Injector, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LiveDiscoveryService } from './live-discovery.service';
import { REMOTE_CLIENT, CONTEXT_PROVIDER } from './tokens';
import { RemoteClient } from './remote-client';

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

    private liveDiscovery = inject(LiveDiscoveryService);
    private remoteClient = inject(REMOTE_CLIENT);
    private contextProvider = inject(CONTEXT_PROVIDER, { optional: true });
    private injector = inject(Injector);

    constructor() {
        // React to configuration changes
        effect(() => {
            // This must be inside an injection context, which constructor is.
        });
    }

    ngOnInit() {
        this.liveDiscovery.monitor(this.remoteName);

        // Initial Load handled by Effect below
        // this.loadMfe();

        // Setup Effect explicitly to watch for updates
        // We use an effect that depends on the signal from the service
        const configSignal = this.liveDiscovery.getRemoteConfig(this.remoteName);

        effect(() => {
            const config = configSignal();
            if (config) {
                console.log(`[MfeHost] Effect triggered for ${this.remoteName}. New Version: ${config.selected.version}, Current: ${this.currentVersion}`);
                // Check if Version OR Flags changed
                // Simple JSON stringify for deep comparison of flags
                const currentFlags = JSON.stringify(this.currentConfig?.resolutionContext?.flags || {});
                const newFlags = JSON.stringify(config.resolutionContext?.flags || {});

                if (config.selected.version !== this.currentVersion || currentFlags !== newFlags) {
                    console.log(`[MfeHost] Change detected for ${this.remoteName}. Reloading...`);
                    this.currentConfig = config; // Update cached config
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
        this.container.clear();
        try {
            const module = await this.remoteClient.loadRemoteModule<any>(
                this.remoteName,
                this.exposedModule,
                {
                    type: 'module',
                    retries: 1,
                    context: this.contextProvider ? this.contextProvider() : {}
                }
            );

            const ComponentType = module.default || Object.values(module)[0];

            if (ComponentType) {
                const ref = this.container.createComponent(ComponentType);

                // Pass inputs
                if (this.inputs) {
                    Object.assign(ref.instance as object, this.inputs);
                }

                // Get current version from service to track state
                const config = this.liveDiscovery.getRemoteConfig(this.remoteName)();
                if (config) {
                    this.currentVersion = config.selected.version;
                }
            } else {
                console.error(`[MfeHost] No component found in exposed module ${this.exposedModule}`);
            }

        } catch (err) {
            console.error(`[MfeHost] Failed to load ${this.remoteName}`, err);
        }
    }

    ngOnDestroy() {
        // Cleanup if needed
    }
}
