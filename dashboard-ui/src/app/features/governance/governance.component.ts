import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Observable, BehaviorSubject, combineLatest } from 'rxjs';
import { map, startWith } from 'rxjs/operators';
import { GovernanceService, PolicyDefinition } from '../../core/services/governance.service';

@Component({
    selector: 'app-governance',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './governance.component.html'
})
export class GovernanceComponent implements OnInit {
    policies$ = new BehaviorSubject<PolicyDefinition[]>([]);
    features$ = new BehaviorSubject<Array<{ key: string, enabled: boolean }>>([]);
    activeTab: 'policies' | 'features' = 'policies';
    searchTerm$ = new BehaviorSubject<string>('');
    filteredPolicies$: Observable<PolicyDefinition[]>;
    filteredFeatures$: Observable<Array<{ key: string, enabled: boolean }>>;

    editingPolicy: PolicyDefinition | null = null;
    isCreating = false;
    editForm = {
        name: '',
        type: '',
        category: '',
        enforcementLevel: 'LOG',
        description: ''
    };

    categories = ['Discovery', 'Routing', 'Security', 'Compatibility', 'Operational'];
    strategyTypes = [
        { key: 'ENV_INTEGRITY', name: 'Environment Integrity' },
        { key: 'SECURITY_VETTING', name: 'Security Vetting' },
        { key: 'MFE_COMPATIBILITY', name: 'Compatibility Check' },
        { key: 'OP_DIRECTIVE', name: 'Operational Directive' },
        { key: 'REGO_OPA', name: 'Rego (External OPA)' }
    ];

    // Metrics
    metrics = {
        activePolicies: 0,
        blockingPolicies: 0,
        activeFeatures: 0
    };

    constructor(private governanceService: GovernanceService) {
        // Setup filtered streams based on the source BehaviorSubjects
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

    private updateMetrics(policies: PolicyDefinition[], features: any[]) {
        this.metrics.activePolicies = policies.length;
        this.metrics.blockingPolicies = policies.filter(x => x.enforcementLevel === 'BLOCK').length;
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
        this.governanceService.toggleFeature(uid).subscribe(newState => {
            this.loadFeatures();
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
            description: ''
        };
    }

    editPolicy(policy: PolicyDefinition) {
        this.isCreating = false;
        this.editingPolicy = policy;
        this.editForm = {
            name: policy.name,
            type: policy.type,
            category: policy.category,
            enforcementLevel: policy.enforcementLevel,
            description: policy.description
        };
    }

    cancelEdit() {
        this.editingPolicy = null;
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
                description: this.editForm.description
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

    setTab(tab: 'policies' | 'features') {
        this.activeTab = tab;
    }

    updateSearch(term: string) {
        this.searchTerm$.next(term);
    }
}
