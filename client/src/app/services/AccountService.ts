import {inject, Injectable, signal} from "@angular/core";
import {HttpClient, HttpParams, HttpResponse} from "@angular/common/http";
import {CreateAccountRequest} from "../request/AccountRequest";
import {catchError, Observable, throwError} from "rxjs";
import {
  accountBlockUnblockMessageResponse,
  CreateAccountResponse,
  getAccountTransactionsResponse
} from "../response/AccountResponse";
import {getAllCardsResponse} from "../response/CardsResponse";

@Injectable({providedIn: 'root'})
export class AccountService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:80/api/v1/accounts';

  userAccounts = signal<CreateAccountResponse[] | null>(null)

  createAccount(accountPayload: CreateAccountRequest): Observable<CreateAccountResponse> {
    return this.http
      .post<CreateAccountResponse>(`${this.apiUrl}`, accountPayload)
      .pipe(catchError((error) => throwError(() => error)));
  }

  getAllAccounts(): Observable<CreateAccountResponse[]> {
    return this.http
      .get<CreateAccountResponse[]>(`${this.apiUrl}/all`)
      .pipe(catchError((error) => throwError(() => error)));
  }

  blockAccount(accountId: string): Observable<accountBlockUnblockMessageResponse> {
    return this.http
      .patch<accountBlockUnblockMessageResponse>(`${this.apiUrl}/${accountId}/block`, null)
      .pipe(catchError((error) => throwError(() => error)));
  }

  unblockAccount(accountId: string): Observable<accountBlockUnblockMessageResponse> {
    return this.http
      .patch<accountBlockUnblockMessageResponse>(`${this.apiUrl}/${accountId}/unblock`, null)
      .pipe(catchError((error) => throwError(() => error)));
  }

  getAccountTransactions(accountId: string, page: number = 0, size: number = 20): Observable<getAccountTransactionsResponse> {

    const params = new HttpParams()
      .set('page', page)
      .set('size', size)

    return this.http
      .get<getAccountTransactionsResponse>(`${this.apiUrl}/${accountId}/transactions`, { params })
      .pipe(catchError((error) => throwError(() => error)));
  }

  exportStatementPdf(accountId: string): Observable<HttpResponse<Blob>> {
    return this.http
      .get(`${this.apiUrl}/${accountId}/statement/pdf`, {
        observe: 'response',
        responseType: 'blob',
      })
      .pipe(catchError((error) => throwError(() => error)));
  }

}
