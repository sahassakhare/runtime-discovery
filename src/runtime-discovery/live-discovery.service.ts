import { Injectable, OnDestroy, signal, computed, inject } from '@angular/core';
import { HttpRuntimeDiscovery } from './runtime-discovery';
import { DISCOVERY_CONFIG } from './tokens';
import { DiscoveryConfig, ResolveRemoteResponse } from './types';
import { interval, Subscription, switchMap, retry, catchError, of } from 'rxjs';

@Injectable({
    providedIn: 'root'
})
export class LiveDiscoveryService implements OnDestroy {
    // Configurable SSE URL
    private eventSource?: EventSource;
    private monitoredRemotes = new Set<string>();

    private subscriptions = new Map<string, Subscription>();

    // Signals to track the latest config for each remote
    private remoteConfigs = signal<Map<string, ResolveRemoteResponse>>(new Map());

    // Dedicated discovery client for polling
    private discoveryClient: HttpRuntimeDiscovery;
    private config = inject(DISCOVERY_CONFIG);

    constructor() {
        this.discoveryClient = new HttpRuntimeDiscovery(this.config.url, this.config.environment, this.config.appName, this.config.tenantId);

        // Initialize SSE Connection
        this.connectSse();
    }

    private connectSse() {
        if (typeof window === 'undefined') return;

        console.log('[LiveDiscovery] Connecting to SSE stream...');
        let url = `${this.config.url}/stream?appName=${this.config.appName}&env=${this.config.environment}`;
        if (this.config.tenantId) {
            url += `&tenantId=${this.config.tenantId}`;
        }

        this.eventSource = new EventSource(url);

        this.eventSource.onmessage = (event) => {
            console.log('[LiveDiscovery] Received SSE event:', event.data);
            if (event.data === 'CONFIG_CHANGED') {
                this.refreshAll();
            }
        };

        this.eventSource.onerror = (err) => {
            console.warn('[LiveDiscovery] SSE Connection lost. Retrying in 5s...', err);
            this.eventSource?.close();
            setTimeout(() => this.connectSse(), 5000);
        };
    }

    ngOnDestroy() {
        this.subscriptions.forEach(sub => sub.unsubscribe());
        this.eventSource?.close();
    }

    /**
     * Starts monitoring a specific remote for updates.
     * If an update is detected, it updates the signal.
     */
    monitor(remoteName: string) {
        if (this.monitoredRemotes.has(remoteName)) {
            return;
        }
        this.monitoredRemotes.add(remoteName);

        // Initial fetch
        this.pollRemote(remoteName).then(config => {
            if (config) this.updateConfigIfChanged(remoteName, config);
        });
    }

    private refreshAll() {
        console.log('[LiveDiscovery] Refreshing all monitored remotes...');
        this.monitoredRemotes.forEach(remote => {
            this.pollRemote(remote).then(config => {
                if (config) this.updateConfigIfChanged(remote, config);
            });
        });
    }

    /**
     * Returns a Signal for the specific remote's configuration.
     */
    getRemoteConfig(remoteName: string) {
        return computed(() => this.remoteConfigs().get(remoteName));
    }

    private async pollRemote(remoteName: string): Promise<ResolveRemoteResponse | null> {
        try {
            return await this.discoveryClient.resolveRemote(remoteName);
        } catch (e) {
            return null;
        }
    }

    private updateConfigIfChanged(remoteName: string, newConfig: ResolveRemoteResponse) {
        const currentMap = this.remoteConfigs();
        const currentConfig = currentMap.get(remoteName);

        if (!currentConfig) {
            // First time seeing it
            const newMap = new Map(currentMap);
            newMap.set(remoteName, newConfig);
            this.remoteConfigs.set(newMap);
            return;
        }

        // Compare integrity or version
        const currentVersion = currentConfig.selected.version;
        const newVersion = newConfig.selected.version;
        const currentEntry = currentConfig.selected.remoteEntry;
        const newEntry = newConfig.selected.remoteEntry;

        const currentFlags = JSON.stringify(currentConfig.resolutionContext?.flags || {});
        const newFlags = JSON.stringify(newConfig.resolutionContext?.flags || {});

        if (currentVersion !== newVersion || currentEntry !== newEntry || currentFlags !== newFlags) {
            console.log(`[LiveDiscovery] Update detected for ${remoteName}: ${currentVersion} (Flags: ${newFlags})`);
            const newMap = new Map(currentMap);
            newMap.set(remoteName, newConfig);
            this.remoteConfigs.set(newMap);
        }
    }
}
