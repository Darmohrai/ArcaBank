import {inject, Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {catchError, Observable, throwError} from "rxjs";
import {ExchangeRateResponse} from "../response/ExchangeRatesResponse";


@Injectable({providedIn: 'root'})
export class ExchangeRatesService {
  private http = inject(HttpClient)
  private apiUrl = 'http://localhost:80/api/v1/exchange-rates';

  getCurrencyRate(): Observable<ExchangeRateResponse[]> {
    return this.http
      .get<ExchangeRateResponse[]>(this.apiUrl)
      .pipe(catchError((error) => throwError(() => error)));
  }

}
