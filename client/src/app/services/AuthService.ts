import { inject, Injectable, PLATFORM_ID } from '@angular/core';
import { RegisterRequest } from '../request/RegisterRequest';
import { BehaviorSubject, catchError, Observable, tap, throwError } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { LoginRequest } from '../request/LoginRequest';
import { LoginResponse } from '../response/LoginResponse';
import { isPlatformBrowser } from '@angular/common';
import {Router} from "@angular/router";
import {AccountService} from "./AccountService";
import {CardService} from "./CardService";

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost/api/v1/auth/public';
  private platformId = inject(PLATFORM_ID);
  private router = inject(Router);
  private AccountService = inject(AccountService)
  private CardService = inject(CardService)

  private readonly ACCESS_KEY = 'auth_token';
  private readonly REFRESH_KEY = 'auth_refresh_token';
  private readonly EXPIRES_AT_KEY = 'auth_token_expires_at';

  private loggedIn = new BehaviorSubject<boolean>(this.hasToken());

  private clearStoredTokens(): void {
    localStorage.removeItem(this.ACCESS_KEY);
    localStorage.removeItem(this.REFRESH_KEY);
    localStorage.removeItem(this.EXPIRES_AT_KEY);

    sessionStorage.removeItem(this.ACCESS_KEY);
    sessionStorage.removeItem(this.REFRESH_KEY);
    sessionStorage.removeItem(this.EXPIRES_AT_KEY);
  }

  register(userData: RegisterRequest): Observable<unknown> {
    return this.http
      .post(`${this.apiUrl}/register`, userData, { responseType: 'text' })
      .pipe(catchError((error) => throwError(() => error)));
  }

  signIn(credentials: LoginRequest, rememberMe: boolean): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/login`, credentials).pipe(
      tap((res) => {
        if (isPlatformBrowser(this.platformId)) {
          const expiresAt = Date.now() + res.expires_in * 1000;
          const storage = rememberMe ? localStorage : sessionStorage;

          // Avoid "stale token in the other storage" bugs
          this.clearStoredTokens();

          storage.setItem(this.ACCESS_KEY, res.access_token);
          storage.setItem(this.REFRESH_KEY, res.refresh_token);
          storage.setItem(this.EXPIRES_AT_KEY, String(expiresAt));
          this.AccountService.getAllAccounts().subscribe(accounts => {
            this.AccountService.userAccounts.set(accounts);
            console.log(this.AccountService.userAccounts());
          })

          this.CardService.getAllCards().subscribe(cards => {
            this.CardService.userCards.set(cards);
            console.log(this.CardService.userCards());
          })
        }
        this.loggedIn.next(true);
        console.log(this.loggedIn.value);
      })
    );
  }

  signOut(): void {
    if (isPlatformBrowser(this.platformId)) {
      this.clearStoredTokens();
    }
    this.loggedIn.next(false);
    console.log(this.loggedIn.value);
    this.router.navigateByUrl('/login');

  }

  private hasToken(): boolean {
    if (!isPlatformBrowser(this.platformId)) return false;
    return !!sessionStorage.getItem(this.ACCESS_KEY) || !!localStorage.getItem(this.ACCESS_KEY);
  }

  isLoggedIn(): boolean {
    return this.loggedIn.value;
  }
}
