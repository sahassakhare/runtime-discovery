export class FeatureClient {
    constructor(private featureServiceUrl: string) { }

    /**
     * Fetches all feature flags for the given context.
     * @param context User context (e.g. userId, roles)
     */
    async fetchFlags(context: Record<string, any> = {}): Promise<Record<string, boolean>> {
        try {
            const response = await fetch(`${this.featureServiceUrl}/api/features/evaluate`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(context)
            });

            if (!response.ok) {
                throw new Error(`Failed to fetch flags: ${response.statusText}`);
            }

            const flags = await response.json();

            // Store globally for synchronous access by MFEs
            if (typeof window !== 'undefined') {
                (window as any).__MAVERICK_FLAGS__ = flags;
                window.dispatchEvent(new CustomEvent('maverick:flags_updated', { detail: flags }));
            }

            return flags;
        } catch (err) {
            console.error('[FeatureClient] Error fetching flags', err);
            return {}; // Fail safe
        }
    }

    /**
     * Synchronously gets a flag value (requires fetchFlags to have been called).
     */
    getFlag(key: string, defaultValue = false): boolean {
        if (typeof window !== 'undefined') {
            const flags = (window as any).__MAVERICK_FLAGS__ || {};
            return flags[key] ?? defaultValue;
        }
        return defaultValue;
    }
}
