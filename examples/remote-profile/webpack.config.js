const { shareAll, withModuleFederationPlugin } = require('@angular-architects/module-federation/webpack');
// const DashboardPlugin = require('@module-federation/dashboard-plugin');


const pkg = require('./package.json');
const version = pkg.version || '1.0.0';

module.exports = {
    experiments: {
        outputModule: true
    },
    plugins: [
        // new DashboardPlugin({
        //     dashboardURL: 'http://localhost:8081/api/dashboard/register',
        //     metadata: {
        //         source: {
        //             url: 'http://localhost:4201'
        //         },
        //         remote: 'remote-profile',
        //         version: version
        //     }
        // })
    ],
    ...withModuleFederationPlugin({
        name: 'remote-profile',
        filename: 'remoteEntry.js',
        library: { type: 'module' },
        exposes: {
            './ProfileComponent': './src/app/profile/profile.component.ts',
        },
        shared: {
            ...shareAll({ singleton: true, strictVersion: true, requiredVersion: 'auto' }),
            "@maverick/runtime-discovery": { singleton: true, strictVersion: false },
        },
    }),
};
