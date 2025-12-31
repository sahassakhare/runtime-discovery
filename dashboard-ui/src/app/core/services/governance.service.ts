import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface PolicyDefinition {
    id: string;
    name: string;
    category: string;
    description: string;
    enforcementLevel: string;
    type: string;
}

@Injectable({
    providedIn: 'root'
})
export class GovernanceService {
    // Hardcoded for dev/demo; in prod use relative path or config token
    private apiUrl = 'http://localhost:8081/api/governance';

    constructor(private http: HttpClient) { }

    getCatalog(): Observable<PolicyDefinition[]> {
        return this.http.get<PolicyDefinition[]>(`${this.apiUrl}/catalog`);
    }

    getFeatures(): Observable<Record<string, boolean>> {
        return this.http.get<Record<string, boolean>>(`${this.apiUrl}/features`);
    }

    toggleFeature(uid: string): Observable<boolean> {
        return this.http.post<boolean>(`${this.apiUrl}/features/${uid}/toggle`, {});
    }

    updatePolicy(id: string, updates: { enforcementLevel?: string, description?: string }): Observable<void> {
        return this.http.put<void>(`${this.apiUrl}/policies/${id}`, updates);
    }

    createPolicy(policy: any): Observable<void> {
        return this.http.post<void>(`${this.apiUrl}/policies`, policy);
    }
}
