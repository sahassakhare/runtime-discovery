import { Provider, ResolutionDetails, EvaluationContext, JsonValue, ResolutionReason, StandardResolutionReasons } from '@openfeature/web-sdk';

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

    resolveBooleanEvaluation(flagKey: string, defaultValue: boolean, context: EvaluationContext): ResolutionDetails<boolean> {
        const value = this.getFlag(flagKey);

        if (typeof value === 'boolean') {
            return {
                value,
                reason: StandardResolutionReasons.TARGETING_MATCH
            };
        }

        return {
            value: defaultValue,
            reason: value === undefined ? StandardResolutionReasons.DEFAULT : StandardResolutionReasons.ERROR
        };
    }

    resolveStringEvaluation(flagKey: string, defaultValue: string, context: EvaluationContext): ResolutionDetails<string> {
        const value = this.getFlag(flagKey);

        if (typeof value === 'string') {
            return {
                value,
                reason: StandardResolutionReasons.TARGETING_MATCH
            };
        }

        return {
            value: defaultValue,
            reason: value === undefined ? StandardResolutionReasons.DEFAULT : StandardResolutionReasons.ERROR
        };
    }

    resolveNumberEvaluation(flagKey: string, defaultValue: number, context: EvaluationContext): ResolutionDetails<number> {
        const value = this.getFlag(flagKey);

        if (typeof value === 'number') {
            return {
                value,
                reason: StandardResolutionReasons.TARGETING_MATCH
            };
        }

        return {
            value: defaultValue,
            reason: value === undefined ? StandardResolutionReasons.DEFAULT : StandardResolutionReasons.ERROR
        };
    }

    resolveObjectEvaluation<T extends JsonValue>(flagKey: string, defaultValue: T, context: EvaluationContext): ResolutionDetails<T> {
        const value = this.getFlag(flagKey);

        if (value && typeof value === 'object') {
            return {
                value: value as T,
                reason: StandardResolutionReasons.TARGETING_MATCH
            };
        }

        return {
            value: defaultValue,
            reason: value === undefined ? StandardResolutionReasons.DEFAULT : StandardResolutionReasons.ERROR
        };
    }

    // Hook for context changes - not needed since we drive from window state
    onContextChange(oldContext: EvaluationContext, newContext: EvaluationContext): Promise<void> {
        return Promise.resolve();
    }
}
