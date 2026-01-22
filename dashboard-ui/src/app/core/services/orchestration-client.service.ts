import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Component {
    id: string;
    type: string;
    title?: string;
    name?: string; // For MFE
    remote?: string; // For MFE
    exposedModule?: string; // For MFE
    props?: Record<string, any>;
    children?: string[]; // IDs of children
}

export interface SurfaceUpdate {
    root: string;
    components: Component[];
}

export interface UiResponse {
    a2uiVersion: string;
    surfaceUpdate: SurfaceUpdate;
}

@Injectable({
    providedIn: 'root'
})
export class OrchestrationClientService {
    // Assuming backend is proxy pass or direct. For dev: localhost:8081
    private apiUrl = 'http://localhost:8081/api/orchestrate';

    constructor(private http: HttpClient) { }

    orchestrate(intent: string): Observable<UiResponse> {
        return this.http.post<UiResponse>(this.apiUrl, { intent });
    }
}
