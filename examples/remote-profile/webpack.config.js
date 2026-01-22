const { shareAll, withModuleFederationPlugin } = require('@angular-architects/module-federation/webpack');

const mfConfig = withModuleFederationPlugin({
    name: 'profile',
    exposes: {
        './Profile': './src/app/profile/profile.component.ts',
        './ProfileLifecycle': './src/app/remote-entry.ts', // Advanced Lifecycle Entry
    },
    shared: {
        ...shareAll({ singleton: true, strictVersion: true, requiredVersion: 'auto' }),
    },
});

module.exports = {
    ...mfConfig,
    output: {
        ...mfConfig.output,
        uniqueName: "profile",
        publicPath: "http://localhost:4201/",
        scriptType: "text/javascript"
    },
    plugins: [
        ...(mfConfig.plugins || []),
        new (require('../../webpack/discovery-client-plugin').DiscoveryClientPlugin)({
            dashboardURL: 'http://localhost:8081/api/dashboard/register',
            metadata: {
                source: {
                    url: 'http://localhost:4201'
                },
                remote: 'profile'
            }
        })
    ]
};
