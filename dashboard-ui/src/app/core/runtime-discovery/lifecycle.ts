/**
 * Protocol for Remote Module Lifecycle.
 * Allows a remote module to control its own initialization, rendering, and cleanup.
 * This is framework-agnostic (works with Angular, React, Vue, etc.).
 */
export interface RemoteLifecycle {
    /**
     * Optional: Check if the remote is ready to be mounted.
     * Use this to prefetch data, check feature flags, or validate permissions before rendering.
     * If this returns false or rejects, the host may choose to show a loading state or error.
     */
    isReady?(): Promise<boolean>;

    /**
     * Required: Mount the remote UI into the provided container.
     * @param container The native DOM element to render into.
     * @param props Input properties, configuration, and context passed from the host.
     */
    mount(container: HTMLElement, props: Record<string, any>): Promise<void>;

    /**
     * Required: Unmount the remote UI and clean up DOM event listeners.
     * @param container The native DOM element (same as passed to mount).
     */
    unmount(container: HTMLElement): Promise<void>;

    /**
     * Optional: Final cleanup of non-DOM resources (e.g., global stores, caches, subscriptions).
     * Called when the remote is being permanently removed or the shell is shutting down.
     */
    dispose?(): Promise<void>;
}
