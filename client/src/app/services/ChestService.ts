import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { catchError, Observable, throwError } from 'rxjs';
import {
  ChestCreationRequest,
  ChestDepositRequest,
  EscrowInitiationRequest,
  VoteRequest,
} from '../request/ChestRequest';
import {
  ChestCreationResponse,
  ChestDepositResponse,
  ChestDetailResponse,
  ChestListResponse,
  EscrowInitiationResponse,
  MessageResponse,
  PendingEscrowResponse,
} from '../response/ChestResponse';

@Injectable({ providedIn: 'root' })
export class ChestService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:80/api/v1/chests';

  getMyChests(): Observable<ChestListResponse[]> {
    return this.http
      .get<ChestListResponse[]>(this.apiUrl)
      .pipe(catchError((error) => throwError(() => error)));
  }

  getChestDetails(chestId: string): Observable<ChestDetailResponse> {
    return this.http
      .get<ChestDetailResponse>(`${this.apiUrl}/${chestId}`)
      .pipe(catchError((error) => throwError(() => error)));
  }

  getPendingEscrow(chestId: string): Observable<PendingEscrowResponse> {
    return this.http
      .get<PendingEscrowResponse>(`${this.apiUrl}/${chestId}/escrow/pending`)
      .pipe(catchError((error) => throwError(() => error)));
  }

  createChest(payload: ChestCreationRequest): Observable<ChestCreationResponse> {
    return this.http
      .post<ChestCreationResponse>(this.apiUrl, payload)
      .pipe(catchError((error) => throwError(() => error)));
  }

  depositToChest(
    chestId: string,
    payload: ChestDepositRequest
  ): Observable<ChestDepositResponse> {
    return this.http
      .post<ChestDepositResponse>(`${this.apiUrl}/${chestId}/deposit`, payload)
      .pipe(catchError((error) => throwError(() => error)));
  }

  initiateWithdrawal(
    chestId: string,
    payload: EscrowInitiationRequest
  ): Observable<EscrowInitiationResponse> {
    return this.http
      .post<EscrowInitiationResponse>(`${this.apiUrl}/${chestId}/escrow/initiate`, payload)
      .pipe(catchError((error) => throwError(() => error)));
  }

  voteEscrow(escrowId: string, payload: VoteRequest): Observable<MessageResponse> {
    return this.http
      .post<MessageResponse>(`${this.apiUrl}/escrow/${escrowId}/vote`, payload)
      .pipe(catchError((error) => throwError(() => error)));
  }
}
