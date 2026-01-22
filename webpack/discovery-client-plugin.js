const http = require('http');
const https = require('https');
const { URL } = require('url');
const fs = require('fs');

class DiscoveryClientPlugin {
    constructor(options) {
        this.options = options;
        this.debugLog = '/Users/sahassakhare/Downloads/mfe-discovery-client/logs/plugin-debug.log';
        this.log(`DiscoveryClientPlugin initialized for ${options.metadata.remote}`);
    }

    log(msg) {
        fs.appendFileSync(this.debugLog, `[${new Date().toISOString()}] ${msg}\n`);
    }

    apply(compiler) {
        const { dashboardURL, metadata } = this.options;
        this.log(`apply() called for ${metadata.remote}`);

        // Attempt to extract exposed modules and remotes from ModuleFederationPlugin
        let exposedNames = [];
        let consumedRemotes = [];
        let exposedMap = {};

        const mfPlugin = compiler.options.plugins.find(p =>
            p.constructor.name === 'ModuleFederationPlugin' ||
            p.constructor.name === 'ModuleFederationPluginV1' ||
            p.constructor.name === 'ModuleFederationPluginV2'
        );

        if (mfPlugin && mfPlugin.options) {
            if (mfPlugin.options.exposes) {
                exposedMap = mfPlugin.options.exposes;
                exposedNames = Object.keys(mfPlugin.options.exposes);
            }
            if (mfPlugin.options.remotes) {
                consumedRemotes = Object.keys(mfPlugin.options.remotes).map(key => ({
                    remoteName: key,
                    dynamic: false
                }));
            }
        }

        this.log(`Started registration for ${metadata.remote}`);

        const payload = JSON.stringify({
            name: metadata.remote,
            version: metadata.version || '1.0.0',
            remoteEntry: metadata.source.url + (metadata.filename || '/remoteEntry.js'),
            exposedModules: exposedNames.map(name => ({
                name,
                filePath: exposedMap[name]
            })),
            consumedRemotes: consumedRemotes
        });

        this.log(`Payload: ${payload}`);

        const url = new URL(dashboardURL);
        const options = {
            hostname: url.hostname,
            port: url.port,
            path: url.pathname,
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Content-Length': Buffer.byteLength(payload),
            },
        };

        const req = (url.protocol === 'https:' ? https : http).request(options, (res) => {
            let body = '';
            res.on('data', (chunk) => body += chunk);
            res.on('end', () => {
                this.log(`Success: ${res.statusCode} ${body}`);
                console.log(`[DiscoveryClientPlugin] Registered ${metadata.remote} successfully.`);
            });
        });

        req.on('error', (e) => {
            this.log(`Error: ${e.message}`);
            console.error(`[DiscoveryClientPlugin] Error registering ${metadata.remote}: ${e.message}`);
        });

        req.write(payload);
        req.end();
    }
}

module.exports = { DiscoveryClientPlugin };
