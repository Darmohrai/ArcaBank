import { Component, inject, OnInit } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { NgForOf, NgIf, DecimalPipe } from '@angular/common';
import { ToastModule } from 'primeng/toast';
import { MessageService } from 'primeng/api';
import { catchError, finalize, throwError } from 'rxjs';

import { AccountService } from '../../services/AccountService';
import { TransfersService } from '../../services/TransfersService';
import { ExchangeRatesService} from '../../services/ExchangeRatesService';
import { CreateAccountResponse } from '../../response/AccountResponse';
import {ExchangeRateResponse} from "../../response/ExchangeRatesResponse";

@Component({
  selector: 'app-exchange',
  standalone: true,
  imports: [ReactiveFormsModule, NgForOf, NgIf, ToastModule, DecimalPipe],
  providers: [MessageService],
  templateUrl: './exchange-page.component.html',
  styleUrl: './exchange-page.component.css',
})
export class ExchangePageComponent implements OnInit {
  private accountService = inject(AccountService);
  private transfersService = inject(TransfersService);
  private exchangeRatesService = inject(ExchangeRatesService);
  private messageService = inject(MessageService);

  accounts: CreateAccountResponse[] = [];
  rates: ExchangeRateResponse[] = [];

  isLoading = true;
  isSubmitting = false;
  serverError: string | null = null;

  // Estimated result shown to user before submit
  estimatedResult: string | null = null;

  exchangeForm = new FormGroup({
    fromAccountId: new FormControl<string>('', {
      nonNullable: true,
      validators: [Validators.required],
    }),
    toAccountId: new FormControl<string>('', {
      nonNullable: true,
      validators: [Validators.required],
    }),
    amount: new FormControl<number | null>(null, {
      validators: [Validators.required, Validators.min(0.01)],
    }),
  });

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.isLoading = true;
    this.accountService.getAllAccounts().subscribe({
      next: (accounts) => {
        this.accounts = accounts;
        this.exchangeRatesService.getCurrencyRate().subscribe({
          next: (rates) => {
            this.rates = rates;
            this.isLoading = false;
          },
          error: () => { this.isLoading = false; },
        });
      },
      error: () => { this.isLoading = false; },
    });
  }

  get fromAccount(): CreateAccountResponse | undefined {
    return this.accounts.find(a => a.id === this.exchangeForm.controls.fromAccountId.value);
  }

  get toAccount(): CreateAccountResponse | undefined {
    return this.accounts.find(a => a.id === this.exchangeForm.controls.toAccountId.value);
  }

  get toAccounts(): CreateAccountResponse[] {
    const fromId = this.exchangeForm.controls.fromAccountId.value;
    const fromAcc = this.accounts.find(a => a.id === fromId);
    if (!fromAcc) return this.accounts.filter(a => a.id !== fromId);
    // exclude same currency accounts
    return this.accounts.filter(a => a.id !== fromId && a.currency !== fromAcc.currency);
  }

  onFromAccountChange(): void {
    this.exchangeForm.controls.toAccountId.setValue('');
    this.estimatedResult = null;
    this.serverError = null;
    this.updateValidators();
    this.recalcEstimate();
  }

  onToAccountChange(): void {
    this.serverError = null;
    this.recalcEstimate();
  }

  onAmountChange(): void {
    this.updateValidators();
    this.recalcEstimate();
  }

  updateValidators(): void {
    const balance = this.fromAccount?.balance ?? 0;
    this.exchangeForm.controls.amount.setValidators([
      Validators.required,
      Validators.min(0.01),
      maxBalanceValidator(balance),
    ]);
    this.exchangeForm.controls.amount.updateValueAndValidity();
  }

  recalcEstimate(): void {
    const from = this.fromAccount;
    const to = this.toAccount;
    const amount = this.exchangeForm.controls.amount.value;

    if (!from || !to || !amount || amount <= 0) {
      this.estimatedResult = null;
      return;
    }

    const estimated = this.convertAmount(amount, from.currency, to.currency);
    if (estimated !== null) {
      this.estimatedResult = `≈ ${estimated.toFixed(2)} ${to.currency}`;
    } else {
      this.estimatedResult = null;
    }
  }

  convertAmount(amount: number, fromCurrency: string, toCurrency: string): number | null {
    // Both rates are against a common base (e.g. UAH).
    // We find how much 1 fromCurrency = X baseCurrency (using sellRate),
    // then how much X baseCurrency = Y toCurrency (using buyRate).
    if (fromCurrency === toCurrency) return amount;

    // If one of the currencies IS the base currency (UAH)
    const fromRate = this.rates.find(r => r.currency === fromCurrency);
    const toRate = this.rates.find(r => r.currency === toCurrency);

    // e.g. UAH → USD: toRate.buyRate = UAH per 1 USD → result = amount / buyRate
    if (!fromRate && toRate) {
      return amount / toRate.buyRate;
    }
    // e.g. USD → UAH: fromRate.sellRate = UAH per 1 USD → result = amount * sellRate
    if (fromRate && !toRate) {
      return amount * fromRate.sellRate;
    }
    // e.g. USD → EUR: go through base
    if (fromRate && toRate) {
      const inBase = amount * fromRate.sellRate;
      return inBase / toRate.buyRate;
    }
    return null;
  }

  getAmountError(): string | null {
    const ctrl = this.exchangeForm.controls.amount;
    if (!ctrl.touched || !ctrl.errors) return null;
    if (ctrl.errors['required']) return 'Введіть суму.';
    if (ctrl.errors['min']) return 'Сума повинна бути більше 0.';
    if (ctrl.errors['exceedsBalance']) return 'Сума перевищує баланс рахунку.';
    return null;
  }

  swapAccounts(): void {
    const from = this.exchangeForm.controls.fromAccountId.value;
    const to = this.exchangeForm.controls.toAccountId.value;
    this.exchangeForm.controls.fromAccountId.setValue(to);
    this.exchangeForm.controls.toAccountId.setValue(from);
    this.onFromAccountChange();
  }

  submit(): void {
    this.exchangeForm.markAllAsTouched();
    this.serverError = null;

    if (this.exchangeForm.invalid) return;

    const from = this.fromAccount;
    const to = this.toAccount;
    if (from && to && from.currency === to.currency) {
      this.serverError = 'Конвертація між рахунками однієї валюти неможлива.';
      return;
    }

    this.isSubmitting = true;

    const payload = {
      fromAccountId: this.exchangeForm.controls.fromAccountId.value,
      toAccountId: this.exchangeForm.controls.toAccountId.value,
      amount: this.exchangeForm.controls.amount.value!,
    };

    this.transfersService
      .exchangeFunds(payload)
      .pipe(
        catchError((error) => {
          const msg = error.error?.message;
          if (error.status === 400 || error.status === 422) {
            this.serverError = msg ?? 'Недостатньо коштів або некоректні рахунки.';
          } else {
            this.serverError = 'Сталася помилка. Спробуйте пізніше.';
          }
          return throwError(() => error);
        }),
        finalize(() => (this.isSubmitting = false))
      )
      .subscribe({
        next: (res) => {
          const toCurrency = to?.currency ?? '';
          const estimated = this.convertAmount(payload.amount, from?.currency ?? '', toCurrency);
          const amountStr = res?.message ?? (estimated ? `${estimated.toFixed(2)} ${toCurrency}` : '');

          this.messageService.add({
            severity: 'success',
            summary: 'Успіх!',
            detail: `Конвертація успішна. На ваш рахунок зараховано ${amountStr}`,
            life: 5000,
          });

          this.exchangeForm.reset();
          this.estimatedResult = null;
          this.loadData();
        },
      });
  }
}

function maxBalanceValidator(balance: number) {
  return (control: AbstractControl): ValidationErrors | null => {
    if (control.value !== null && control.value > balance) {
      return { exceedsBalance: true };
    }
    return null;
  };
}
