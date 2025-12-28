import { Injector } from '@angular/core';

export let AppInjector: Injector;

export function setAppInjector(injector: Injector) {
    if (AppInjector) {
        // Already set
        return;
    }
    AppInjector = injector;
}
