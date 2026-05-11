import {inject, Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {CreateAccountRequest} from "../request/AccountRequest";
import {catchError, throwError} from "rxjs";

@Injectable({ providedIn: 'root' })
export class AccountService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost/api/v1/accounts';


  createAccount(accountPayload: CreateAccountRequest) {
    return this.http
      .post(`${this.apiUrl}`, accountPayload, {responseType: 'text'})
      .pipe(catchError((error) => throwError(() => error)));
  }

}
