import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface GoalResponse {
  goalId: string | null;
  year: number;
  month: number;
  targetAmount: number;
  totalIncome: number;
  totalExpense: number;
  netIncome: number;
  remainingToGoal: number;
}

export interface UpsertGoalRequest {
  year: number;
  month: number;
  targetAmount: number;
}

@Injectable({
  providedIn: 'root'
})
export class GoalsService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:80/api/v1/goals';

  getGoal(year: number, month: number): Observable<GoalResponse> {
    return this.http.get<GoalResponse>(`${this.apiUrl}/${year}/${month}`);
  }

  saveGoal(request: UpsertGoalRequest): Observable<GoalResponse> {
    return this.http.post<GoalResponse>(this.apiUrl, request);
  }
}
