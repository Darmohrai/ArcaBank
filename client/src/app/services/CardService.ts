import {inject, Injectable, signal} from "@angular/core";
import {HttpClient, HttpParams} from "@angular/common/http";
import {catchError, Observable, throwError} from "rxjs";
import {cardBlockUnblockMessageResponse, createdCardResponse, getAllCardsResponse} from "../response/CardsResponse";
import {createCardRequest} from "../request/CardRequest";



@Injectable({providedIn: 'root'})
export class CardService {

  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:80/api/v1/cards';

  userCards = signal<getAllCardsResponse[] | null>(null)

  getAllCards(): Observable<getAllCardsResponse[]>{
    return this.http
      .get<getAllCardsResponse[]>(`${this.apiUrl}/all`)
      .pipe(catchError((error) => throwError(() => error)));
  }

  getCardById(): Observable<getAllCardsResponse> {
    return this.http
      .get<getAllCardsResponse>(`${this.apiUrl}`)
      .pipe(catchError((error) => throwError(() => error)));
  }

  createCardByAccount(payload:createCardRequest, accountId: string): Observable<createdCardResponse> {
    return this.http
      .post<createdCardResponse>(`${this.apiUrl}/${accountId}/create`, payload)
      .pipe(catchError((error) => throwError(() => error)));
  }

  blockCard(cardId: string): Observable<cardBlockUnblockMessageResponse> {
    return this.http
      .patch<cardBlockUnblockMessageResponse>(`${this.apiUrl}/${cardId}/block`, null)
      .pipe(catchError((error) => throwError(() => error)));
  }

  unblockCard(cardId: string): Observable<cardBlockUnblockMessageResponse> {
    return this.http
      .patch<cardBlockUnblockMessageResponse>(`${this.apiUrl}/${cardId}/unblock`, null)
      .pipe(catchError((error) => throwError(() => error)));
  }

  getCardTransactions(cardId: string, page: number = 0, size: number = 20): Observable<getAllCardsResponse> {
    
    const params = new HttpParams()
      .set('page', page)
      .set('size', size)

    return this.http
      .get<getAllCardsResponse>(`${this.apiUrl}/${cardId}/transactions`, { params })
      .pipe(catchError((error) => throwError(() => error)));
  }

}
