import {inject, Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {TransferExchangeRequest, TransferFundsRequest} from "../request/TransferRequest";
import {catchError, Observable, throwError} from "rxjs";
import {TransferHistoryResponse, TransferResponse} from "../response/TransferResponse";


@Injectable({providedIn: 'root'})
export class TransfersService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:80/api/v1/transfers';

  transferFunds(payload: TransferFundsRequest): Observable<TransferResponse> {
    return this.http
      .post<TransferResponse>(`${this.apiUrl}/transaction`, payload)
      .pipe(catchError((error) => throwError(() => error)));
  }

  exchangeFunds(payload: TransferExchangeRequest): Observable<TransferResponse> {
    return this.http
      .post<TransferResponse>(`${this.apiUrl}/exchange`, payload)
      .pipe(catchError((error) => throwError(() => error)));
  }

  getUserTransactionHistory(): Observable<TransferHistoryResponse> {
    return this.http
      .get<TransferHistoryResponse>(`${this.apiUrl}/history`)
      .pipe(catchError((error) => throwError(() => error)));
  }



}
