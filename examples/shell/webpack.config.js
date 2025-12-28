const { shareAll, withModuleFederationPlugin } = require('@angular-architects/module-federation/webpack');
// const DashboardPlugin = require('@module-federation/dashboard-plugin');

module.exports = {
    plugins: [
        /*
        new DashboardPlugin({
            dashboardURL: 'http://localhost:8081/api/dashboard/register',
            metadata: {
                source: {
                    url: 'http://localhost:4200'
                },
                remote: 'shell'
            }
        })
        */
    ],
    ...withModuleFederationPlugin({
        name: 'shell',
        remotes: {
            // 'remote-profile': 'http://localhost:4201/remoteEntry.js', // We will load dynamic
        },
        shared: {
            ...shareAll({ singleton: true, strictVersion: true, requiredVersion: 'auto' }),
            "@maverick/runtime-discovery": { singleton: true, strictVersion: false },
        },
    }),
};
