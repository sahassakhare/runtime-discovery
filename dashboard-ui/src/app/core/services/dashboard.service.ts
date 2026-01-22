import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Stats, HealthStatus, GovernanceCheck } from '../models/dashboard.model';

@Injectable({
    providedIn: 'root'
})
export class DashboardService {
    private http = inject(HttpClient);
    private apiUrl = 'http://localhost:8081/api/dashboard';

    getStats(): Observable<any> { // Using any for stats as map structure varies
        return this.http.get<any>(`${this.apiUrl}/stats`);
    }

    getHealth(): Observable<HealthStatus[]> {
        return this.http.get<HealthStatus[]>(`${this.apiUrl}/health`);
    }

    getGovernance(): Observable<GovernanceCheck[]> {
        return this.http.get<GovernanceCheck[]>(`${this.apiUrl}/governance`);
    }

    getDeployments(): Observable<any[]> {
        return this.http.get<any[]>(`${this.apiUrl}/deployments`);
    }

    getResolutionGraph(remoteName: string): Observable<any> {
        return this.http.get<any>(`${this.apiUrl}/resolution-graph/${remoteName}`);
    }

    getRuntime(): Observable<any[]> {
        return this.http.get<any[]>(`${this.apiUrl}/runtime`);
    }

    getMfeDetails(name: string): Observable<any> {
        return this.http.get<any>(`${this.apiUrl}/mfe/${name}/details`);
    }

    getDependencyGraph(): Observable<any> {
        return this.http.get<any>(`${this.apiUrl}/dependency-graph`);
    }

    lockVersion(mfeName: string, version: string, environment: string = 'PRODUCTION', locked: boolean = true): Observable<any> {
        return this.http.post(`${this.apiUrl.replace('/dashboard', '')}/deployments/lock`, { mfeName, version, environment, locked });
    }

    getMfeMetrics(name: string): Observable<any> {
        return this.http.get<any>(`${this.apiUrl}/mfe/${name}/metrics`);
    }

    getMfeEnv(name: string): Observable<any> {
        return this.http.get<any>(`${this.apiUrl}/mfe/${name}/env`);
    }

    getMfeLogs(name: string): Observable<string[]> {
        return this.http.get<string[]>(`${this.apiUrl}/mfe/${name}/logs`);
    }

    getMfeThreads(name: string): Observable<any[]> {
        return this.http.get<any[]>(`${this.apiUrl}/mfe/${name}/threads`);
    }

    getMfeTraces(name: string): Observable<any[]> {
        return this.http.get<any[]>(`${this.apiUrl}/mfe/${name}/traces`);
    }
}
