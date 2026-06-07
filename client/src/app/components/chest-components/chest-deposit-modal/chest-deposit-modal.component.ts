import { Component, EventEmitter, inject, Input, Output } from '@angular/core';
import {
  AbstractControl,
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { CurrencyPipe, NgForOf, NgIf } from '@angular/common';
import { DialogModule } from 'primeng/dialog';
import { catchError, finalize, throwError } from 'rxjs';

import { AccountService } from '../../../services/AccountService';
import { ChestService } from '../../../services/ChestService';
import { CreateAccountResponse } from '../../../response/AccountResponse';
import { ChestViewModel } from '../../../response/ChestResponse';

@Component({
  selector: 'app-chest-deposit-modal',
  standalone: true,
  imports: [DialogModule, ReactiveFormsModule, NgForOf, NgIf, CurrencyPipe],
  templateUrl: './chest-deposit-modal.component.html',
  styleUrl: './chest-deposit-modal.component.css',
})
export class ChestDepositModalComponent {
  @Input({ required: true }) set visible(value: boolean) {
    this.dialogVisible = value;
    if (value) {
      this.loadAccounts();
      this.depositForm.reset();
      this.serverError = null;
    }
  }
  @Input({ required: true }) chest!: ChestViewModel;
  @Output() visibleChange = new EventEmitter<boolean>();
  @Output() deposited = new EventEmitter<number>();

  dialogVisible = false;

  private accountService = inject(AccountService);
  private chestService = inject(ChestService);

  accounts: CreateAccountResponse[] = [];
  isLoading = false;
  isSubmitting = false;
  serverError: string | null = null;

  depositForm = new FormGroup({
    senderAccountId: new FormControl<string>('', {
      nonNullable: true,
      validators: [Validators.required],
    }),
    amount: new FormControl<number | null>(null, {
      validators: [Validators.required, Validators.min(0.01)],
    }),
  });

  onDialogVisibleChange(value: boolean): void {
    this.dialogVisible = value;
    this.visibleChange.emit(value);
  }

  loadAccounts(): void {
    this.isLoading = true;
    this.accountService.getAllAccounts().subscribe({
      next: (accounts) => {
        this.accounts = accounts.filter((acc) => acc.iban !== this.chest?.iban);
        this.isLoading = false;
        if (this.accounts.length === 1) {
          this.depositForm.controls.senderAccountId.setValue(this.accounts[0].id);
          this.updateAmountValidator();
        }
      },
      error: () => {
        this.isLoading = false;
      },
    });
  }

  onAccountChange(): void {
    this.updateAmountValidator();
  }

  get selectedAccount(): CreateAccountResponse | undefined {
    return this.accounts.find(
      (a) => a.id === this.depositForm.controls.senderAccountId.value
    );
  }

  updateAmountValidator(): void {
    const balance = this.selectedAccount?.balance ?? 0;
    this.depositForm.controls.amount.setValidators([
      Validators.required,
      Validators.min(0.01),
      maxBalanceValidator(balance),
    ]);
    this.depositForm.controls.amount.updateValueAndValidity();
  }

  getAmountError(): string | null {
    const ctrl = this.depositForm.controls.amount;
    if (!ctrl.touched || !ctrl.errors) return null;
    if (ctrl.errors['required']) return 'Введіть суму.';
    if (ctrl.errors['min']) return 'Сума повинна бути більше 0.';
    if (ctrl.errors['exceedsBalance']) return 'Недостатньо коштів на рахунку.';
    return null;
  }

  close(): void {
    this.onDialogVisibleChange(false);
  }

  submit(): void {
    this.depositForm.markAllAsTouched();
    this.serverError = null;

    if (this.depositForm.invalid) {
      return;
    }

    this.isSubmitting = true;

    this.chestService
      .depositToChest(this.chest.id, {
        senderAccountId: this.depositForm.controls.senderAccountId.value,
        amount: this.depositForm.controls.amount.value!,
      })
      .pipe(
        catchError((error) => {
          const title = error.error?.title as string | undefined;
          const detail = error.error?.detail as string | undefined;

          if (title === 'INSUFFICIENT_FUNDS') {
            this.serverError = 'Недостатньо коштів на рахунку для переказу.';
          } else if (title === 'CHEST_CLOSED') {
            this.serverError = 'Скриня неактивна.';
          } else if (title === 'ACCESS_DENIED' || title === 'NOT_CHEST_ACCESS') {
            this.serverError = 'Немає доступу до цієї скрині.';
          } else {
            this.serverError = detail ?? 'Сталася помилка. Спробуйте пізніше.';
          }
          return throwError(() => error);
        }),
        finalize(() => (this.isSubmitting = false))
      )
      .subscribe({
        next: (response) => {
          this.deposited.emit(response.newBalance);
          this.close();
        },
      });
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
