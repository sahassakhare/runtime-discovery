const express = require('express');
const cors = require('cors');
const bodyParser = require('body-parser');

const app = express();
app.use(cors());
app.use(bodyParser.json());

const PORT = 3000;

// Mock Database of Microfrontends
const REGISTRY = {
    'remote-profile': {
        versions: {
            '1.0.0': {
                url: 'http://localhost:4201/remoteEntry.js',
                integrity: 'sha384-mock-integrity-hash-v1'
            },
            '1.1.0-canary': {
                url: 'http://localhost:4202/remoteEntry.js', // Simulating a different deployment
                integrity: 'sha384-mock-integrity-hash-v2'
            }
        }
    }
};

app.post('/resolve', (req, res) => {
    const { remoteName, context } = req.body;
    console.log(`Resolving ${remoteName} with context:`, context);

    const mfe = REGISTRY[remoteName];
    if (!mfe) {
        return res.status(404).json({ error: 'Remote not found' });
    }

    // DEFAULT: Load v1.0.0
    let selectedVersion = '1.0.0';
    let variant = null;
    let flags = {
        'profile.new-ui': false
    };

    // LOGIC: Context-Aware Routing (Canary)
    // If user is ID '999' OR matches random 10% chance (simulated)
    if (context?.userId === '999' || context?.role === 'beta-tester') {
        selectedVersion = '1.1.0-canary';
        variant = { name: 'canary-v1.1', type: 'canary' };
        flags['profile.new-ui'] = true; // Turn on feature flag for canary
        flags['profile.debug'] = true;
    }

    const versionInfo = mfe.versions[selectedVersion];

    const response = {
        remoteName,
        selected: {
            version: selectedVersion,
            remoteEntry: versionInfo.url,
            integrity: versionInfo.integrity
        },
        // Fallback to stable if canary fails
        fallback: selectedVersion !== '1.0.0' ? {
            version: '1.0.0',
            remoteEntry: mfe.versions['1.0.0'].url
        } : undefined,
        cacheTtlSeconds: 60,
        variant,
        flags
    };

    res.json(response);
});

app.listen(PORT, () => {
    console.log(`Discovery Mock Server running on http://localhost:${PORT}`);
});
