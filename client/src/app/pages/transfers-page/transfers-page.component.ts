import { Component, inject, OnInit } from '@angular/core';
import { AccountService } from '../../services/AccountService';
import { CardService } from '../../services/CardService';
import { TransfersService } from '../../services/TransfersService';
import { MessageService } from 'primeng/api';
import { Router } from '@angular/router';
import {
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators,
  AbstractControl,
  ValidationErrors,
} from '@angular/forms';
import { catchError, finalize, throwError } from 'rxjs';
import { NgForOf, NgIf, CurrencyPipe } from '@angular/common';
import { ToastModule } from 'primeng/toast';
import { CreateAccountResponse } from '../../response/AccountResponse';
import { getAllCardsResponse } from '../../response/CardsResponse';
import { TransferFundsRequest } from '../../request/TransferRequest';

type DestinationType = 'ACCOUNT' | 'CARD';

@Component({
  selector: 'app-transfer-page',
  standalone: true,
  imports: [ReactiveFormsModule, NgForOf, NgIf, ToastModule, CurrencyPipe],
  providers: [MessageService],
  templateUrl: './transfers-page.component.html',
  styleUrl: './transfers-page.component.css',
})
export class TransfersPageComponent implements OnInit {
  private accountService = inject(AccountService);
  private cardService = inject(CardService);
  private transfersService = inject(TransfersService);
  private messageService = inject(MessageService);
  private router = inject(Router);

  accounts: CreateAccountResponse[] = [];
  allCards: getAllCardsResponse[] = [];
  cardsForSelectedAccount: getAllCardsResponse[] = [];

  isSubmitting = false;
  isLoading = true;
  serverError: string | null = null;

  destinationType: DestinationType = 'ACCOUNT';

  transferForm = new FormGroup({
    fromAccountId: new FormControl<string>('', {
      nonNullable: true,
      validators: [Validators.required],
    }),
    fromCardId: new FormControl<string>(''),
    destination: new FormControl<string>('', {
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
        this.cardService.getAllCards().subscribe({
          next: (cards) => {
            this.allCards = cards;
            this.isLoading = false;
          },
          error: () => {
            this.isLoading = false;
          },
        });
      },
      error: () => {
        this.isLoading = false;
      },
    });
  }

  onAccountChange(): void {
    const accountId = this.transferForm.controls.fromAccountId.value;
    this.cardsForSelectedAccount = this.allCards.filter(
      (c) => c.accountId === accountId
    );
    this.transferForm.controls.fromCardId.setValue('');
    this.updateAmountValidator();
  }

  setDestinationType(type: DestinationType): void {
    this.destinationType = type;
    this.transferForm.controls.destination.setValue('');
    this.serverError = null;
  }

  get selectedAccount(): CreateAccountResponse | undefined {
    return this.accounts.find(
      (a) => a.id === this.transferForm.controls.fromAccountId.value
    );
  }

  updateAmountValidator(): void {
    const balance = this.selectedAccount?.balance ?? 0;
    this.transferForm.controls.amount.setValidators([
      Validators.required,
      Validators.min(0.01),
      maxBalanceValidator(balance),
    ]);
    this.transferForm.controls.amount.updateValueAndValidity();
  }

  get sourceId(): string {
    if (this.destinationType === 'CARD') {
      return this.transferForm.controls.fromCardId.value ?? '';
    }
    return this.transferForm.controls.fromAccountId.value;
  }

  get sourceType(): 'CARD' | 'ACCOUNT' {
    return this.destinationType === 'CARD' ? 'CARD' : 'ACCOUNT';
  }

  submit(): void {
    this.transferForm.markAllAsTouched();
    this.serverError = null;

    if (this.transferForm.invalid) return;
    if (this.destinationType === 'CARD' && !this.transferForm.controls.fromCardId.value) {
      return;
    }

    this.isSubmitting = true;

    const payload: TransferFundsRequest = {
      senderSourceId: this.sourceId,
      sourceType: this.sourceType,
      destination: this.transferForm.controls.destination.value,
      amount: this.transferForm.controls.amount.value!,
    };

    this.transfersService
      .transferFunds(payload)
      .pipe(
        catchError((error) => {
          if (error.status === 400) {
            this.serverError =
              error.error?.message ?? 'Недостатньо коштів або некоректний рахунок отримувача.';
          } else if (error.status === 404) {
            this.serverError = 'Рахунок отримувача не знайдено.';
          } else {
            this.serverError = 'Сталася помилка. Спробуйте пізніше.';
          }
          return throwError(() => error);
        }),
        finalize(() => (this.isSubmitting = false))
      )
      .subscribe({
        next: () => {
          this.messageService.add({
            severity: 'success',
            summary: 'Успіх!',
            detail: 'Переказ успішний.',
          });
          this.transferForm.reset();
          this.cardsForSelectedAccount = [];
          setTimeout(() => this.router.navigate(['/transactions']), 1500);
        },
      });
  }

  formatCardNumber(cardNumber: string): string {
    return cardNumber.replace(/(.{4})/g, '$1 ').trim();
  }

  getAmountError(): string | null {
    const ctrl = this.transferForm.controls.amount;
    if (!ctrl.touched || !ctrl.errors) return null;
    if (ctrl.errors['required']) return 'Введіть суму.';
    if (ctrl.errors['min']) return 'Сума повинна бути більше 0.';
    if (ctrl.errors['exceedsBalance']) return 'Сума перевищує баланс рахунку.';
    return null;
  }
}

function maxBalanceValidator(balance: number) {
  return (control: AbstractControl): ValidationErrors | null => {
    const val = control.value;
    if (val !== null && val > balance) {
      return { exceedsBalance: true };
    }
    return null;
  };
}
