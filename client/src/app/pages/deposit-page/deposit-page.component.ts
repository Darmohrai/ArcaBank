import { Component, inject, OnInit } from '@angular/core';
import {
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { NgForOf, NgIf, CurrencyPipe } from '@angular/common';
import { ToastModule } from 'primeng/toast';
import { MessageService } from 'primeng/api';
import { Router } from '@angular/router';
import { catchError, finalize, throwError } from 'rxjs';

import { AccountService } from '../../services/AccountService';
import { TransactionService } from '../../services/TransactionService';
import { CreateAccountResponse } from '../../response/AccountResponse';
import { DepositFundsRequest } from '../../request/TransactionRequest';

@Component({
  selector: 'app-deposit-page',
  standalone: true,
  imports: [ReactiveFormsModule, NgForOf, NgIf, ToastModule, CurrencyPipe],
  providers: [MessageService],
  templateUrl: './deposit-page.component.html',
  styleUrl: './deposit-page.component.css',
})
export class DepositPageComponent implements OnInit {
  private accountService = inject(AccountService);
  private transactionService = inject(TransactionService);
  private messageService = inject(MessageService);
  private router = inject(Router);

  accounts: CreateAccountResponse[] = [];
  isLoading = true;
  isSubmitting = false;
  serverError: string | null = null;

  depositForm = new FormGroup({
    accountId: new FormControl<string>('', {
      nonNullable: true,
      validators: [Validators.required],
    }),
    amount: new FormControl<number | null>(null, {
      validators: [Validators.required, Validators.min(0.01)],
    }),
  });

  ngOnInit(): void {
    this.loadAccounts();
  }

  loadAccounts(): void {
    this.isLoading = true;
    this.accountService.getAllAccounts().subscribe({
      next: (accounts) => {
        this.accounts = accounts;
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
      },
    });
  }

  get selectedAccount(): CreateAccountResponse | undefined {
    return this.accounts.find(
      (a) => a.id === this.depositForm.controls.accountId.value
    );
  }

  getAmountError(): string | null {
    const ctrl = this.depositForm.controls.amount;
    if (!ctrl.touched || !ctrl.errors) return null;
    if (ctrl.errors['required']) return 'Введіть суму.';
    if (ctrl.errors['min']) return 'Сума повинна бути більше 0.';
    return null;
  }

  submit(): void {
    this.depositForm.markAllAsTouched();
    this.serverError = null;

    if (this.depositForm.invalid) return;

    this.isSubmitting = true;

    const payload: DepositFundsRequest = {
      accountId: this.depositForm.controls.accountId.value,
      amount: this.depositForm.controls.amount.value!,
    };

    this.transactionService
      .depositFunds(payload)
      .pipe(
        catchError((error) => {
          const body = error.error;
          const msg =
            typeof body === 'string'
              ? body
              : (body?.message as string | undefined);
          if (error.status === 400 || error.status === 422) {
            this.serverError =
              msg && msg.trim().length > 0
                ? msg
                : 'Некоректна сума або рахунок недоступний.';
          } else if (error.status === 404) {
            this.serverError = 'Рахунок не знайдено.';
          } else {
            this.serverError = 'Сталася помилка. Спробуйте пізніше.';
          }
          return throwError(() => error);
        }),
        finalize(() => (this.isSubmitting = false))
      )
      .subscribe({
        next: (message) => {
          const detail =
            typeof message === 'string' && message.trim().length > 0
              ? message
              : 'Кошти успішно зараховано на рахунок.';
          this.messageService.add({
            severity: 'success',
            summary: 'Успіх!',
            detail,
          });
          this.depositForm.reset();
          this.loadAccounts();
          setTimeout(() => this.router.navigate(['/balances']), 1500);
        },
      });
  }
}
