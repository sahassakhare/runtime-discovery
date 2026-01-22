import { BackendConfig, BackendConfigSchema } from '../schemas/backend-protocol';
import { config } from '../config';

interface CacheState {
    data: BackendConfig | null;
    lastUpdated: number;
}

const state: CacheState = {
    data: null,
    lastUpdated: 0
};

export class ConfigService {

    /**
     * Retrieves valid configuration.
     * Strategy: Cache-First -> Network -> Stale-Fallback -> Default-Fallback
     */
    static async getConfig(): Promise<BackendConfig> {
        const now = Date.now();
        const isExpired = (now - state.lastUpdated) > (config.cacheTtlSeconds * 1000);

        if (state.data && !isExpired) {
            return state.data;
        }

        try {
            console.log('[ConfigService] Fetching fresh config from backend...');
            const freshConfig = await this.fetchWithRetry(`${config.backendUrl}/api/orchestrate/config`);

            // Validate Contract
            const validated = BackendConfigSchema.parse(freshConfig);

            // Update Cache
            state.data = validated;
            state.lastUpdated = now;
            console.log(`[ConfigService] Config updated. Registry size: ${validated.registry.length}`);

            return validated;
        } catch (error) {
            console.error('[ConfigService] Failed to fetch/validate config:', error);

            if (state.data) {
                console.warn('[ConfigService] Serving STALE config due to backend failure.');
                return state.data;
            }

            // Critical Fail (Cold Start + Backend Down)
            // Return Minimal Fallback to keep service alive
            console.error('[ConfigService] No cache available. Using emergency fallback.');
            return {
                template: '', // Will trigger fallback logic in Flow
                registry: [
                    { remoteName: 'remote-profile', exposedModule: './ProfileComponent', capabilities: ['profile.view'] } // Emergency MFE
                ]
            };
        }
    }

    private static async fetchWithRetry(url: string, retries = 3): Promise<any> {
        for (let i = 0; i < retries; i++) {
            try {
                const res = await fetch(url);
                if (!res.ok) throw new Error(`HTTP ${res.status}`);
                return await res.json();
            } catch (err) {
                if (i === retries - 1) throw err;
                const delay = 500 * Math.pow(2, i); // Exponential Backoff
                await new Promise(r => setTimeout(r, delay));
            }
        }
    }
}
