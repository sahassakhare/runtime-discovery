import { Server } from "@modelcontextprotocol/sdk/server/index.js";
import { StdioServerTransport } from "@modelcontextprotocol/sdk/server/stdio.js";
import {
    CallToolRequestSchema,
    ListToolsRequestSchema,
} from "@modelcontextprotocol/sdk/types.js";
import axios from "axios";

// Configuration
const API_BASE_URL = "http://localhost:8081/api";

// Initialize Server
const server = new Server(
    {
        name: "mfe-orchestration-server",
        version: "1.0.0",
    },
    {
        capabilities: {
            tools: {},
        },
    }
);

// Define Tools
server.setRequestHandler(ListToolsRequestSchema, async () => {
    return {
        tools: [
            {
                name: "resolve_mfe",
                description: "Resolve a Microfrontend's remote URL and capabilities based on name and environment.",
                inputSchema: {
                    type: "object",
                    properties: {
                        remoteName: {
                            type: "string",
                            description: "The name of the remote MFE (e.g., 'remote-profile')",
                        },
                        environment: {
                            type: "string",
                            description: "Environment (PRODUCTION, STAGING, DEVELOPMENT)",
                            default: "PRODUCTION"
                        },
                    },
                    required: ["remoteName"],
                },
            },
            {
                name: "orchestrate_ui",
                description: "Orchestrate a UI response based on natural language intent using the Enterprise Agent Mesh.",
                inputSchema: {
                    type: "object",
                    properties: {
                        intent: {
                            type: "string",
                            description: "The user's business intent (e.g., 'Show me my profile')",
                        },
                    },
                    required: ["intent"],
                },
            },
        ],
    };
});

// Handle Tool Execution
server.setRequestHandler(CallToolRequestSchema, async (request) => {
    const { name, arguments: args } = request.params;

    try {
        if (name === "resolve_mfe") {
            const { remoteName, environment = "PRODUCTION" } = args as any;
            const response = await axios.post(`${API_BASE_URL}/resolve`, {
                remoteName,
                environment
            });
            return {
                content: [{ type: "text", text: JSON.stringify(response.data, null, 2) }],
            };
        }

        if (name === "orchestrate_ui") {
            const { intent } = args as any;
            const response = await axios.post(`${API_BASE_URL}/orchestrate`, {
                intent
            });
            return {
                content: [{ type: "text", text: JSON.stringify(response.data, null, 2) }],
            };
        }

        throw new Error(`Tool not found: ${name}`);
    } catch (error: any) {
        const errorMessage = error.response
            ? `API Error: ${error.response.status} - ${JSON.stringify(error.response.data)}`
            : `Error: ${error.message}`;

        return {
            content: [{ type: "text", text: errorMessage }],
            isError: true,
        };
    }
});

// Start Server
async function run() {
    const transport = new StdioServerTransport();
    await server.connect(transport);
    console.error("MFE Orchestration MCP Server running on stdio");
}

run().catch((error) => {
    console.error("Fatal error running server:", error);
    process.exit(1);
});
