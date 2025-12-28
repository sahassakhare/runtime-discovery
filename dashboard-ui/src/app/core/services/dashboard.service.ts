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
}
