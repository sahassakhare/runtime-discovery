import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Observable, BehaviorSubject, combineLatest } from 'rxjs';
import { map } from 'rxjs/operators';
import { GovernanceService, PolicyDefinition } from '../../core/services/governance.service';

// Angular Material Imports
import { MatTabsModule } from '@angular/material/tabs';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatChipsModule } from '@angular/material/chips';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';

@Component({
    selector: 'app-governance',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        MatTabsModule,
        MatTableModule,
        MatButtonModule,
        MatIconModule,
        MatSidenavModule,
        MatFormFieldModule,
        MatInputModule,
        MatSelectModule,
        MatChipsModule,
        MatTooltipModule,
        MatSlideToggleModule
    ],
    templateUrl: './governance.component.html',
    styles: [`
        .drawer-header {
            padding: 32px 40px;
            border-bottom: 1px solid #f1f5f9;
        }
        .drawer-title {
            font-size: 20px;
            font-weight: 900;
            color: #0f172a;
            letter-spacing: -0.02em;
        }
        .drawer-subtitle {
            font-size: 10px;
            font-weight: 900;
            color: #3b82f6;
            text-transform: uppercase;
            letter-spacing: 0.2em;
            margin-top: 4px;
        }
        .section-label {
            display: flex;
            align-items: center;
            gap: 12px;
            margin-bottom: 24px;
        }
        .section-label .bar {
            width: 3px;
            height: 16px;
            background: #3b82f6;
            border-radius: 4px;
        }
        .section-label span {
            font-size: 11px;
            font-weight: 900;
            color: #475569;
            text-transform: uppercase;
            letter-spacing: 0.15em;
        }
        .config-editor-wrapper {
            background: #0f172a;
            border-radius: 16px;
            padding: 2px;
            box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.1);
        }
        .config-editor {
            width: 100%;
            height: 400px;
            font-family: 'JetBrains Mono', monospace;
            font-size: 13px;
            background: #1e293b;
            color: #e2e8f0;
            padding: 24px;
            border: none;
            border-radius: 14px;
            resize: none;
            outline: none;
            line-height: 1.6;
        }
        .editor-footer {
            display: flex;
            justify-content: flex-end;
            margin-top: 12px;
        }
        .env-badge {
            background: #f1f5f9;
            color: #475569;
            padding: 4px 12px;
            border-radius: 6px;
            font-size: 9px;
            font-weight: 900;
            text-transform: uppercase;
            letter-spacing: 0.1em;
            border: 1px solid #e2e8f0;
        }
        ::ng-deep .drawer-form .mat-mdc-form-field-wrapper {
            background: transparent !important;
            border: none !important;
            box-shadow: none !important;
        }
        ::ng-deep .drawer-form .mat-mdc-text-field-wrapper {
            background: #f8fafc !important;
            border: 1px solid #e2e8f0 !important;
        }
        ::ng-deep .drawer-form .mat-focused .mat-mdc-text-field-wrapper {
            border-color: #3b82f6 !important;
            background: #fff !important;
        }

        /* REFINED LAYOUT ENGINE */
        .content-area-zen { padding-top: 0; }
        .g-search-container { position: relative; width: 300px; }
        .g-search-input { width: 100%; padding-left: 44px; padding-right: 16px; padding-top: 10px; padding-bottom: 10px; background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 12px; font-size: 13px; font-weight: 600; outline: none; transition: all 0.2s; height: 44px; }
        .g-search-input:focus { background: #fff; border-color: #2563eb; box-shadow: 0 0 0 4px rgba(37, 99, 235, 0.1); }
        .g-search-icon { position: absolute; left: 16px; top: 50%; transform: translateY(-50%); color: #94a3b8; font-size: 18px; width: 18px; height: 18px; z-index: 1; }
        .g-search-shortcuts { position: absolute; right: 16px; top: 50%; transform: translateY(-50%); display: flex; gap: 4px; opacity: 0; transition: opacity 0.2s; }
        .g-search-container:focus-within .g-search-shortcuts { opacity: 1; }

        .g-metrics-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 32px; padding: 40px; border-bottom: 1px solid #f1f5f9; background: rgba(248, 250, 252, 0.5); }
        .g-metric-card { display: flex; align-items: center; gap: 20px; }
        .g-metric-icon-box { width: 48px; height: 48px; border-radius: 16px; background: #fff; border: 1px solid #e2e8f0; display: flex; align-items: center; justify-content: center; box-shadow: 0 1px 2px rgba(0,0,0,0.05); }
        .g-metric-content { display: flex; flex-direction: column; gap: 2px; }
        .g-metric-label { font-size: 9px; font-weight: 900; color: #94a3b8; text-transform: uppercase; letter-spacing: 0.1em; }
        .g-metric-value { font-size: 20px; font-weight: 900; color: #0f172a; font-family: 'JetBrains Mono', monospace; line-height: 1; }

        .g-status-pill { padding: 12px 20px; background: #fff; border: 1px solid #e2e8f0; border-radius: 16px; display: flex; align-items: center; gap: 16px; justify-self: flex-end; }
        .g-status-text { display: flex; flex-direction: column; align-items: flex-end; }
        .g-pulse-dot { width: 6px; height: 6px; border-radius: 50%; background: #10b981; animation: pulse 2s infinite; }

        ::ng-deep .mat-mdc-tab-header {
            --mdc-tab-indicator-active-indicator-color: #3b82f6;
            --mat-tab-header-active-label-text-color: #3b82f6;
            --mat-tab-header-inactive-label-text-color: #64748b;
            font-weight: 800;
            text-transform: uppercase;
            letter-spacing: 0.1em;
            font-size: 11px;
        }
        ::ng-deep .mat-mdc-tab-body-wrapper {
            padding: 24px 0;
        }
        @keyframes pulse { 0% { opacity: 1; } 50% { opacity: 0.4; } 100% { opacity: 1; } }
    `]
})
export class GovernanceComponent implements OnInit {
    policies$ = new BehaviorSubject<PolicyDefinition[]>([]);
    features$ = new BehaviorSubject<Array<{ key: string, enabled: boolean }>>([]);
    searchTerm$ = new BehaviorSubject<string>('');
    filteredPolicies$: Observable<PolicyDefinition[]>;
    filteredFeatures$: Observable<Array<{ key: string, enabled: boolean }>>;

    displayedPolicyColumns: string[] = ['name', 'scope', 'engine', 'status', 'admin'];
    displayedFeatureColumns: string[] = ['flag', 'execution', 'control'];

    editingPolicy: PolicyDefinition | null = null;
    isCreating = false;
    editForm = {
        name: '',
        type: '',
        category: '',
        enforcementLevel: 'LOG',
        description: '',
        configuration: '{}'
    };

    categories = ['Discovery', 'Routing', 'Security', 'Compatibility', 'Operational'];
    strategyTypes = [
        { key: 'ENV_INTEGRITY', name: 'Environment Integrity' },
        { key: 'SECURITY_VETTING', name: 'Security Vetting' },
        { key: 'MFE_COMPATIBILITY', name: 'Compatibility Check' },
        { key: 'OP_DIRECTIVE', name: 'Operational Directive' },
        { key: 'REGO_OPA', name: 'Rego (External OPA)' }
    ];

    enforcementLevels = ['BLOCK', 'LOG', 'ALLOW', 'WARNING'];

    // Metrics
    metrics = {
        activePolicies: 0,
        blockingPolicies: 0,
        activeFeatures: 0
    };

    constructor(private governanceService: GovernanceService) {
        this.filteredPolicies$ = combineLatest([
            this.policies$,
            this.searchTerm$
        ]).pipe(
            map(([policies, term]: [PolicyDefinition[], string]) => policies.filter(p =>
                p.name.toLowerCase().includes(term.toLowerCase()) ||
                p.id.toLowerCase().includes(term.toLowerCase()) ||
                p.category.toLowerCase().includes(term.toLowerCase())
            ))
        );

        this.filteredFeatures$ = combineLatest([
            this.features$,
            this.searchTerm$
        ]).pipe(
            map(([features, term]: [any[], string]) => features.filter(f =>
                f.key.toLowerCase().includes(term.toLowerCase())
            ))
        );
    }

    get allPoliciesActive(): boolean {
        const policies = this.policies$.value;
        return policies.length > 0 && policies.every(p => p.active);
    }

    get allFeaturesActive(): boolean {
        const features = this.features$.value;
        return features.length > 0 && features.every(f => f.enabled);
    }

    private updateMetrics(policies: PolicyDefinition[], features: any[]) {
        this.metrics.activePolicies = policies.filter(x => x.active).length;
        this.metrics.blockingPolicies = policies.filter(x => x.active && x.enforcementLevel === 'BLOCK').length;
        this.metrics.activeFeatures = features.filter(x => x.enabled).length;
    }

    ngOnInit(): void {
        this.loadPolicies();
        this.loadFeatures();
    }

    loadPolicies() {
        this.governanceService.getCatalog().subscribe(p => {
            this.policies$.next(p);
            this.updateMetrics(p, this.features$.value);
        });
    }

    loadFeatures() {
        this.governanceService.getFeatures().pipe(
            map(features => Object.entries(features).map(([key, enabled]) => ({ key, enabled })))
        ).subscribe(list => {
            this.features$.next(list);
            this.updateMetrics(this.policies$.value, list);
        });
    }

    toggleFeature(uid: string) {
        this.governanceService.toggleFeature(uid).subscribe(() => {
            this.loadFeatures();
        });
    }

    onTogglePolicy(policy: PolicyDefinition, active: boolean) {
        this.governanceService.togglePolicy(policy.id, active).subscribe(() => {
            this.loadPolicies();
        });
    }

    toggleAllPolicies(active: boolean) {
        // Optimistic UI Update
        const updated = this.policies$.value.map(p => ({ ...p, active }));
        this.policies$.next(updated);

        this.governanceService.toggleAllPolicies(active).subscribe({
            next: () => this.loadPolicies(),
            error: () => this.loadPolicies() // Revert on error
        });
    }

    toggleAllFeatures(active: boolean) {
        // Optimistic UI Update
        const updated = this.features$.value.map(f => ({ ...f, enabled: active }));
        this.features$.next(updated);

        this.governanceService.toggleAllFeatures(active).subscribe({
            next: () => this.loadFeatures(),
            error: () => this.loadFeatures() // Revert on error
        });
    }

    openCreateModal() {
        this.isCreating = true;
        this.editingPolicy = null;
        this.editForm = {
            name: '',
            type: this.strategyTypes[0].key,
            category: this.categories[0],
            enforcementLevel: 'LOG',
            description: '',
            configuration: '{}'
        };
    }

    editPolicy(policy: PolicyDefinition) {
        this.isCreating = false;
        this.editingPolicy = policy;

        // Ensure we show at least a valid template if config is missing
        const config = policy.configuration && policy.configuration.trim() !== ''
            ? policy.configuration
            : this.getDefaultConfigForType(policy.type);

        this.editForm = {
            name: policy.name,
            type: policy.type,
            category: policy.category,
            enforcementLevel: policy.enforcementLevel,
            description: policy.description,
            configuration: this.formatJson(config)
        };
    }

    private getDefaultConfigForType(type: string): string {
        const templates: Record<string, string> = {
            'ENV_INTEGRITY': '{\n  "restricted_env": "PRODUCTION",\n  "restricted_channels": ["CANARY"]\n}',
            'COMPATIBILITY': '{\n  "blocked_prefixes": ["0."]\n}',
            'SECURITY_ABAC': '{\n  "target_mfe": "remote-audit",\n  "required_role": "ADMIN"\n}',
            'OPERATIONAL': '{\n  "flag_key": "maintenance-mode",\n  "bypass_internal": true\n}',
            'REGO_OPA': 'package system.auth\n\ndefault allow = false\n\nallow {\n    input.user.role == "admin"\n}'
        };
        return templates[type] || '{}';
    }

    private formatJson(json: string): string {
        if (!json || typeof json !== 'string' || json.trim() === '') return '{}';
        try {
            // If it's already a pretty string, just return it
            if (json.includes('\n') && json.includes('    ')) return json;

            const obj = JSON.parse(json);
            return JSON.stringify(obj, null, 4);
        } catch (e) {
            // It might be Rego or non-JSON, return as is
            return json;
        }
    }

    savePolicy() {
        if (this.isCreating) {
            this.governanceService.createPolicy(this.editForm).subscribe(() => {
                this.isCreating = false;
                this.loadPolicies();
            });
        } else if (this.editingPolicy) {
            this.governanceService.updatePolicy(this.editingPolicy.id, {
                enforcementLevel: this.editForm.enforcementLevel,
                description: this.editForm.description,
                configuration: this.editForm.configuration
            }).subscribe(() => {
                this.editingPolicy = null;
                this.loadPolicies();
            });
        }
    }

    closeDrawer() {
        this.isCreating = false;
        this.editingPolicy = null;
    }

    prettifyJson() {
        this.editForm.configuration = this.formatJson(this.editForm.configuration);
    }

    copyToClipboard() {
        navigator.clipboard.writeText(this.editForm.configuration);
    }

    updateSearch(term: string) {
        this.searchTerm$.next(term);
    }
}
