import { z } from 'zod';

// Define the MFE object structure expected from Java Backend
const MfeSchema = z.object({
    remoteName: z.string(),
    exposedModule: z.string(),
    capability: z.string().optional(),
    capabilities: z.array(z.string()).optional()
});

// Define the full Config Response structure
export const BackendConfigSchema = z.object({
    template: z.string().min(10, "Template must be a valid prompt string"),
    registry: z.array(MfeSchema)
});

export type BackendConfig = z.infer<typeof BackendConfigSchema>;
