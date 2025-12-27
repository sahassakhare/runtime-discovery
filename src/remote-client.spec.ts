import { RemoteClient } from './remote-client';
import { HttpRuntimeDiscovery } from './runtime-discovery';

describe('RemoteClient', () => {
    it('should be defined', () => {
        const discovery = new HttpRuntimeDiscovery('http://localhost', 'test');
        const client = new RemoteClient(discovery);
        expect(client).toBeDefined();
    });
});
