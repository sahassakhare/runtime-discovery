import dotenv from 'dotenv';
dotenv.config();

export const config = {
    port: parseInt(process.env.PORT || '3000', 10),
    backendUrl: process.env.BACKEND_URL || 'http://localhost:8081',
    cacheTtlSeconds: parseInt(process.env.CACHE_TTL_SECONDS || '60', 10),
    logLevel: process.env.LOG_LEVEL || 'info'
};

if (!process.env.BACKEND_URL) {
    console.warn('[Config] BACKEND_URL not set in .env, using default: ' + config.backendUrl);
}
