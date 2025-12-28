import { Component, OnInit, Injector } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, RouterLink } from '@angular/router';
import { setAppInjector } from './app-injector';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent implements OnInit {
  title = 'shell';
  flags = {};

  constructor(private injector: Injector) {
    setAppInjector(this.injector);
  }

  ngOnInit() {
    // Listen for flag updates from the library
    window.addEventListener('maverick:flags_updated', (e: any) => {
      this.flags = e.detail;
      console.log('Shell received flags:', this.flags);
    });
  }
}
