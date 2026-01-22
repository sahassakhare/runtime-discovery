import * as http from 'http';
import * as https from 'https';

export class DiscoveryClientPlugin {
    private options: any;

    constructor(options: any) {
        this.options = options;
    }

    apply(compiler: any) {
        compiler.hooks.done.tapAsync('DiscoveryClientPlugin', (stats: any, callback: () => void) => {
            const { dashboardURL, metadata } = this.options;

            // Extract exposed modules from webpack config if not provided
            const exposed = compiler.options.plugins.find((p: any) => p.constructor.name === 'ModuleFederationPlugin' || p.constructor.name === 'ModuleFederationPluginV1')?.options?.exposes || {};
            const exposedNames = Object.keys(exposed);

            // Extract remotes
            const remotes = compiler.options.plugins.find((p: any) => p.constructor.name === 'ModuleFederationPlugin' || p.constructor.name === 'ModuleFederationPluginV1')?.options?.remotes || {};
            const consumedRemotes = Object.keys(remotes).map(key => ({
                remoteName: key,
                dynamic: false
            }));

            const payload = JSON.stringify({
                name: metadata.remote,
                version: metadata.version || '1.0.0',
                remoteEntry: metadata.source.url + (metadata.filename || '/remoteEntry.js'),
                exposedModules: exposedNames.map(name => ({
                    name,
                    filePath: exposed[name]
                })),
                consumedRemotes: consumedRemotes
            });

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
                res.on('data', () => { });
                res.on('end', () => {
                    console.log(`[DiscoveryClientPlugin] Registered ${metadata.remote} successfully.`);
                    callback();
                });
            });

            req.on('error', (e) => {
                console.error(`[DiscoveryClientPlugin] Error registering ${metadata.remote}: ${e.message}`);
                callback();
            });

            req.write(payload);
            req.end();
        });
    }
}
