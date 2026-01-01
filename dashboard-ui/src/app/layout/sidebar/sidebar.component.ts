import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';

@Component({
    selector: 'app-sidebar',
    standalone: true,
    imports: [CommonModule, RouterLink, RouterLinkActive, MatListModule, MatIconModule, MatDividerModule],
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
