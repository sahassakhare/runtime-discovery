const { shareAll, withModuleFederationPlugin } = require('@angular-architects/module-federation/webpack');

const mfConfig = withModuleFederationPlugin({
    name: 'shell',
    remotes: {
        // 'remote-profile': 'http://localhost:4201/remoteEntry.js', // We will load dynamic
    },
    shared: {
        ...shareAll({ singleton: true, strictVersion: true, requiredVersion: 'auto' }),
        "@maverick/runtime-discovery": { singleton: true, strictVersion: false },
    },
});

module.exports = {
    ...mfConfig,
    plugins: [
        ...(mfConfig.plugins || []),
        new (require('../../webpack/discovery-client-plugin').DiscoveryClientPlugin)({
            dashboardURL: 'http://localhost:8081/api/dashboard/register',
            metadata: {
                source: {
                    url: 'http://localhost:4200'
                },
                remote: 'shell'
            }
        })
    ]
};
