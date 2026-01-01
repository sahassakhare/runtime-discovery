import { Injectable, signal } from '@angular/core';

@Injectable({
    providedIn: 'root'
})
export class AuthService {
    private _isLoggedIn = signal(false);

    isLoggedIn = this._isLoggedIn.asReadonly();

    login() {
        this._isLoggedIn.set(true);
    }

    logout() {
        this._isLoggedIn.set(false);
    }

    getContext() {
        return {
            'user.authenticated': String(this._isLoggedIn())
        };
    }
}
