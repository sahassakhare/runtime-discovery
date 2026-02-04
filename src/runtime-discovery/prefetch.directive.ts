import { Directive, ElementRef, HostListener, Input, OnInit } from '@angular/core';
import { RemoteClient } from './remote-client';

/**
 * A directive that enables smart prefetching of microfrontends.
 * 
 * It monitors user interaction (hover) or browser idle state to predictively load
 * the remote module's script, warming the cache before the user actually navigates.
 * 
 * @example
 * ```html
 * <!-- Prefetch on hover (default) -->
 * <a routerLink="/profile" [mfePrefetch]="'profile'">Profile</a>
 * 
 * <!-- Prefetch when browser is idle -->
 * <button [mfePrefetch]="'reports'" [prefetchTrigger]="'idle'">Reports</button>
 * ```
 */
@Directive({
    selector: '[mfePrefetch]',
    standalone: true
})
export class PrefetchDirective implements OnInit {
    /** The name of the remote to prefetch, as registered in the MFE Registry */
    @Input('mfePrefetch') remoteName!: string;

    /** The exposed module to load (defaults to './Module') */
    @Input() exposedModule: string = './Module';

    /** 
     * The trigger strategy:
     * - 'hover': Triggers when mouse enters the element.
     * - 'idle': Triggers when the browser reports it is idle (via requestIdleCallback).
     */
    @Input() prefetchTrigger: 'hover' | 'idle' = 'hover';

    /**
     * The type of remote artifact to load.
     * defaults to 'module'.
     */
    @Input() remoteType: 'module' | 'script' | 'manifest' = 'module';

    private hasPrefetched = false;

    constructor(private el: ElementRef, private client: RemoteClient) { }

    ngOnInit() {
        if (this.prefetchTrigger === 'idle' && typeof window !== 'undefined' && 'requestIdleCallback' in window) {
            (window as any).requestIdleCallback(() => this.triggerPrefetch());
        }
    }

    @HostListener('mouseenter')
    onMouseEnter() {
        if (this.prefetchTrigger === 'hover') {
            this.triggerPrefetch();
        }
    }

    private triggerPrefetch() {
        if (this.hasPrefetched || !this.remoteName) return;

        this.hasPrefetched = true;
        this.client.preload(this.remoteName, this.exposedModule, { type: this.remoteType })
            .catch(err => console.debug(`[Prefetch] Failed for ${this.remoteName}`, err));
    }
}
