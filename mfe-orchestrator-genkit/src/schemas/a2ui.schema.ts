import { z } from 'zod';

// Base Component Properties
const BaseComponentResponse = z.object({
    id: z.string().describe('Unique identifier for the component'),
    title: z.string().optional().describe('Display title for headers/sections'),
});

// 1. Page Component
const PageComponent = BaseComponentResponse.extend({
    type: z.literal('a2ui.v1.Page'),
    children: z.array(z.string()).describe('List of child component IDs'),
});

// 2. MFE Component (Strict Schema)
const MfeComponent = BaseComponentResponse.extend({
    type: z.literal('a2ui.v1.Mfe'),
    remote: z.string().describe('Must match a valid remoteName from the registry'),
    exposedModule: z.string().describe('Must match the exposed module path'),
    props: z.record(z.any()).describe('Input properties for the MFE (business data)'),
});

// 3. Section Component (New)
const SectionComponent = BaseComponentResponse.extend({
    type: z.literal('a2ui.v1.Section'),
    children: z.array(z.string()).describe('List of child component IDs'),
    props: z.object({
        layout: z.enum(['stack', 'grid', 'dashboard', 'wizard']).optional().describe('Adaptive layout mode'),
        columns: z.number().optional().describe('Number of columns for grid/dashboard layouts'),
    }).optional(),
});

// 4. Text Component
const TextComponent = BaseComponentResponse.extend({
    type: z.literal('a2ui.v1.Text'),
    props: z.object({
        text: z.string().optional(),
        valuePath: z.string().optional().describe('JSON Pointer to dataModel for reactive text'),
    }),
});

// 5. Button Component
const ButtonComponent = BaseComponentResponse.extend({
    type: z.literal('a2ui.v1.Button'),
    props: z.object({
        label: z.string(),
        command: z.string().describe('Backend command ID to execute'),
    }),
});

// 6. Table Component (New)
const TableComponent = BaseComponentResponse.extend({
    type: z.literal('a2ui.v1.Table'),
    props: z.object({
        columns: z.array(z.string()).describe('List of column headers'),
        dataSource: z.string().describe('JSON Pointer to dataModel array (e.g. /employees)'),
    }),
});

// Union of all components
export const ComponentSchema = z.discriminatedUnion('type', [
    PageComponent,
    MfeComponent,
    SectionComponent,
    TextComponent,
    ButtonComponent,
    TableComponent
]);

// Top Level Response Schema
export const UiResponseSchema = z.object({
    a2uiVersion: z.literal('1.2'),
    telemetry: z.object({
        traceId: z.string(),
        agent: z.string(),
        policyMatched: z.string(),
    }),
    surfaceUpdate: z.object({
        root: z.string(),
        components: z.array(ComponentSchema),
    }),
    dataModelUpdate: z.object({
        paths: z.record(z.any()),
    }).optional(),
});

export type UiResponse = z.infer<typeof UiResponseSchema>;
