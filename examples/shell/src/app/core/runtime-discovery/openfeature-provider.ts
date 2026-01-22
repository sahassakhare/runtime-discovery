import { Provider } from '@openfeature/web-sdk';

/**
 * OpenFeatureProvider
 * 
 * An OpenFeature Provider that reads flags from the global window object.
 * These flags are populated by the Maverick RemoteClient during module resolution.
 */
export class OpenFeatureProvider implements Provider {
    readonly metadata = {
        name: 'Maverick OpenFeature Provider',
    };

    /**
     * Helper to retrieve flag from window
     */
    private getFlag(flagKey: string): any {
        if (typeof window === 'undefined') {
            return undefined;
        }
        const flags = (window as any).__MAVERICK_FLAGS__ || {};
        return flags[flagKey];
    }

    resolveBooleanEvaluation(flagKey: string, defaultValue: boolean, context: any): any {
        const value = this.getFlag(flagKey);

        if (typeof value === 'boolean') {
            return {
                value,
                reason: 'TARGETING_MATCH' as any
            };
        }

        return {
            value: defaultValue,
            reason: value === undefined ? 'DEFAULT' as any : 'ERROR' as any
        };
    }

    resolveStringEvaluation(flagKey: string, defaultValue: string, context: any): any {
        const value = this.getFlag(flagKey);

        if (typeof value === 'string') {
            return {
                value,
                reason: 'TARGETING_MATCH' as any
            };
        }

        return {
            value: defaultValue,
            reason: value === undefined ? 'DEFAULT' as any : 'ERROR' as any
        };
    }

    resolveNumberEvaluation(flagKey: string, defaultValue: number, context: any): any {
        const value = this.getFlag(flagKey);

        if (typeof value === 'number') {
            return {
                value,
                reason: 'TARGETING_MATCH' as any
            };
        }

        return {
            value: defaultValue,
            reason: value === undefined ? 'DEFAULT' as any : 'ERROR' as any
        };
    }

    resolveObjectEvaluation<T extends any>(flagKey: string, defaultValue: T, context: any): any {
        const value = this.getFlag(flagKey);

        if (value && typeof value === 'object') {
            return {
                value: value as T,
                reason: 'TARGETING_MATCH' as any
            };
        }

        return {
            value: defaultValue,
            reason: value === undefined ? 'DEFAULT' as any : 'ERROR' as any
        };
    }

    // Hook for context changes - not needed since we drive from window state
    onContextChange(oldContext: any, newContext: any): Promise<void> {
        return Promise.resolve();
    }
}
