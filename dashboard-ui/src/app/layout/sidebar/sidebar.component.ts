import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive } from '@angular/router';

@Component({
    selector: 'app-sidebar',
    standalone: true,
    imports: [CommonModule, RouterLink, RouterLinkActive],
    templateUrl: './sidebar.component.html',
    styleUrl: './sidebar.component.css'
})
export class SidebarComponent {
    navItems = [
        { label: 'Overview', icon: 'dashboard', route: '/overview' },
        { label: 'Deployments', icon: 'layers', route: '/deployments' },
        { label: 'Resolution', icon: 'hub', route: '/resolution' },
        { label: 'Governance', icon: 'policy', route: '/governance' },
        { label: 'Settings', icon: 'settings', route: '/settings' }
    ];
}
