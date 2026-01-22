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
    dataModelUpdate?: {
        paths: Record<string, any>;
    };
}

@Injectable({
    providedIn: 'root'
})
export class OrchestrationClientService {
    // Switching to Genkit Sidecar (Node.js)
    private apiUrl = 'http://localhost:3000/orchestrate';

    constructor(private http: HttpClient) { }

    orchestrate(intent: string): Observable<UiResponse> {
        return this.http.post<UiResponse>(this.apiUrl, { intent });
    }
}
