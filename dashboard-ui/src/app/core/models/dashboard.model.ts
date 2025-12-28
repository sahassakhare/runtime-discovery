export interface Stats {
    totalMfes: number;
    activeVersions: number;
    deploymentsToday: number;
    avgLighthouseScore: number;
}

export interface HealthStatus {
    name: string;
    status: 'HEALTHY' | 'DEGRADED' | 'DOWN';
    uptime: string;
}

export interface GovernanceCheck {
    check: string;
    status: 'PASS' | 'WARN' | 'FAIL';
}

export interface Deployment {
    id: number;
    name: string;
    type: string;
    group: string;
    activeVersion: string;
    status: string;
}

export interface GraphNode {
    id: string;
    label: string;
    type: 'root' | 'strategy' | 'version';
}

export interface GraphEdge {
    source: string;
    target: string;
    label?: string;
}

export interface ResolutionGraph {
    nodes: GraphNode[];
    edges: GraphEdge[];
}

export interface RuntimeMetric {
    mfeName: string;
    versionSkew: string;
    clientErrors: string;
    serverErrors: string;
    latency: string;
}
