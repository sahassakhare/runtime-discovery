import express from 'express';
import cors from 'cors';
import { orchestratorFlow } from './flows/orchestrator.flow';

import * as dotenv from 'dotenv'; // Load env vars

dotenv.config();

const app = express();
app.use(express.json());
app.use(cors({ origin: '*' })); // Allow all for dev

// Health Check
app.get('/', (req, res) => {
    res.json({
        status: 'UP',
        service: 'mfe-orchestrator-genkit',
        version: '1.0.0',
        endpoints: ['POST /orchestrate']
    });
});

const PORT = 3000;

import { ConfigService } from './services/config-service';
import { config } from './config';

app.post('/orchestrate', async (req, res) => {
    const start = Date.now();
    const traceId = `trace-${Math.random().toString(36).substr(2, 9)}`;
    const { intent, userContext } = req.body;

    // Structured Log: Request Start
    console.log(JSON.stringify({
        level: 'info',
        msg: 'Processing Request',
        traceId,
        intent,
        timestamp: new Date().toISOString()
    }));

    try {
        // Robust Config Fetch
        const { template, registry } = await ConfigService.getConfig();
        const configTime = Date.now() - start;

        const result = await orchestratorFlow({
            intent,
            userContext: userContext || { role: 'Admin' },
            discoveredMFEs: registry,
            promptTemplate: template
        });

        const totalTime = Date.now() - start;

        // Structured Log: Success
        console.log(JSON.stringify({
            level: 'info',
            msg: 'Orchestration Complete',
            traceId,
            configLatency: configTime,
            totalLatency: totalTime,
            mfeCount: registry.length
        }));

        res.json(result);

    } catch (error) {
        console.error(JSON.stringify({
            level: 'error',
            msg: 'Orchestration Failed',
            traceId,
            error: error instanceof Error ? error.message : String(error)
        }));
        res.status(500).json({ error: 'Internal Server Error', traceId });
    }
});

app.listen(PORT, () => {
    console.log(`🚀 Genkit Orchestrator running on http://localhost:${PORT}`);
});
