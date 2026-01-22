import { genkit, z } from 'genkit';
import { googleAI, geminiPro } from '@genkit-ai/googleai';
import { UiResponseSchema } from '../schemas/a2ui.schema';

const ai = genkit({
    plugins: [googleAI()], // Revert to default (likely v1beta which 2.0 needs)
    model: 'gemini-2.0-flash-exp',
});

export const orchestratorFlow = ai.defineFlow(
    {
        name: 'orchestratorFlow',
        inputSchema: z.object({
            intent: z.string(),
            userContext: z.record(z.any()),
            discoveredMFEs: z.array(z.record(z.any())).optional(),
            promptTemplate: z.string().optional(),
        }),
        outputSchema: UiResponseSchema,
    },
    async (input) => {
        const { intent, userContext, discoveredMFEs, promptTemplate } = input;

        let systemPrompt = '';

        if (promptTemplate) {
            // Dynamic Prompt from DB with variable injection
            const discoveryJson = JSON.stringify({ registry: { discoveredMFEs: discoveredMFEs || [] } }, null, 2);
            const identityJson = JSON.stringify(userContext || {}, null, 2);

            systemPrompt = promptTemplate
                .replace('{{USER_INTENT}}', intent)
                .replace('{{DISCOVERY_BLOCK}}', discoveryJson)
                .replace('{{IDENTITY_BLOCK}}', identityJson)
                // Add extra instructions for Genkit if missing in Java prompt
                + "\n\nCRITICAL: RETURN ONLY RAW JSON. NO MARKDOWN.";
        } else {
            // Fallback prompt (simplified)
            systemPrompt = `You are an Enterprise UI Orchestrator. Intent: "${intent}". MFEs: ${JSON.stringify(discoveredMFEs)}`;
        }

        console.log(`[Flow] Generating with model: googleai/gemini-2.0-flash-exp`);

        // Use 'json' format but NOT strict schema mode to avoid null-fallbacks
        const { text } = await ai.generate({
            model: 'googleai/gemini-2.0-flash-exp',
            prompt: systemPrompt,
            output: { format: 'json' },
        });

        console.log('[Flow] Raw Model Output:', text);

        if (!text) {
            throw new Error('Empry response from LLM');
        }

        // Clean up markdown code blocks if present (common issue)
        const cleanJson = text.replace(/```json/g, '').replace(/```/g, '').trim();
        const parsedOutput = JSON.parse(cleanJson);

        return parsedOutput;
    }
);
