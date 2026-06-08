import {HttpClient} from "@angular/common/http";
import {inject, Injectable} from "@angular/core";
import {catchError, Observable, throwError} from "rxjs";
import {DepositFundsRequest} from "../request/TransactionRequest";

export interface MonthlyStats {
  month: number;
  income: number;
  expense: number;
}

@Injectable({providedIn: 'root'})
export class TransactionService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:80/api/v1/transactions';
  private apiUrlT = 'http://localhost:80/api/v1/transfers';

  depositFunds(payload: DepositFundsRequest) {
    return this.http
      .post(`${this.apiUrl}/deposit`, payload, {responseType: "text"})
      .pipe(catchError((error) => throwError(() => error)));
  }

  getMonthlyStats(): Observable<MonthlyStats[]> {
    return this.http.get<MonthlyStats[]>(`${this.apiUrlT}/stats`)
      .pipe(catchError((error) => throwError(() => error)));
  }

}
