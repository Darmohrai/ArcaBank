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

type DestinationMode = 'OWN_ACCOUNT' | 'MANUAL';
type ManualDestinationType = 'ACCOUNT' | 'CARD';

@Component({
  selector: 'app-chest-withdrawal-modal',
  standalone: true,
  imports: [DialogModule, ReactiveFormsModule, NgForOf, NgIf, CurrencyPipe],
  templateUrl: './chest-withdrawal-modal.component.html',
  styleUrl: './chest-withdrawal-modal.component.css',
})
export class ChestWithdrawalModalComponent {
  @Input({ required: true }) set visible(value: boolean) {
    this.dialogVisible = value;
    if (value) {
      this.loadAccounts();
      this.withdrawForm.reset();
      this.destinationMode = 'OWN_ACCOUNT';
      this.manualDestinationType = 'ACCOUNT';
      this.serverError = null;
      this.updateAmountValidator();
    }
  }
  @Input({ required: true }) chest!: ChestViewModel;
  @Output() visibleChange = new EventEmitter<boolean>();
  @Output() initiated = new EventEmitter<void>();

  dialogVisible = false;

  private accountService = inject(AccountService);
  private chestService = inject(ChestService);

  accounts: CreateAccountResponse[] = [];
  isLoading = false;
  isSubmitting = false;
  serverError: string | null = null;
  destinationMode: DestinationMode = 'OWN_ACCOUNT';
  manualDestinationType: ManualDestinationType = 'ACCOUNT';

  withdrawForm = new FormGroup({
    amount: new FormControl<number | null>(null, {
      validators: [Validators.required, Validators.min(0.01)],
    }),
    purpose: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required],
    }),
    destinationAccountId: new FormControl<string>('', {
      nonNullable: true,
    }),
    manualDestination: new FormControl<string>('', {
      nonNullable: true,
    }),
  });

  get availableBalance(): number {
    return this.chest?.currentBalance ?? 0;
  }

  get destinationValue(): string {
    if (this.destinationMode === 'OWN_ACCOUNT') {
      return this.withdrawForm.controls.destinationAccountId.value;
    }

    return normalizeManualDestination(
      this.withdrawForm.controls.manualDestination.value
    );
  }

  get isFormInvalid(): boolean {
    return this.withdrawForm.invalid || !this.destinationValue || !!this.getDestinationError();
  }

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
      },
      error: () => {
        this.isLoading = false;
      },
    });
  }

  setDestinationMode(mode: DestinationMode): void {
    this.destinationMode = mode;
    this.withdrawForm.controls.destinationAccountId.setValue('');
    this.withdrawForm.controls.manualDestination.setValue('');
    this.serverError = null;
  }

  setManualDestinationType(type: ManualDestinationType): void {
    this.manualDestinationType = type;
    this.withdrawForm.controls.manualDestination.setValue('');
    this.serverError = null;
  }

  updateAmountValidator(): void {
    const balance = this.availableBalance;
    this.withdrawForm.controls.amount.setValidators([
      Validators.required,
      Validators.min(0.01),
      maxBalanceValidator(balance),
    ]);
    this.withdrawForm.controls.amount.updateValueAndValidity();
  }

  getAmountError(): string | null {
    const ctrl = this.withdrawForm.controls.amount;
    if (!ctrl.touched || !ctrl.errors) return null;
    if (ctrl.errors['required']) return 'Введіть суму.';
    if (ctrl.errors['min']) return 'Сума повинна бути більше 0.';
    if (ctrl.errors['exceedsBalance']) return 'Сума перевищує доступний баланс.';
    return null;
  }

  getDestinationError(): string | null {
    const ownAccountCtrl = this.withdrawForm.controls.destinationAccountId;
    const manualCtrl = this.withdrawForm.controls.manualDestination;

    if (this.destinationMode === 'OWN_ACCOUNT') {
      if (!ownAccountCtrl.touched || ownAccountCtrl.value) return null;
      return 'Оберіть рахунок отримувача.';
    }

    if (!manualCtrl.touched) return null;
    const value = normalizeManualDestination(manualCtrl.value);
    if (!value) return 'Введіть рахунок або номер картки.';
    if (this.manualDestinationType === 'ACCOUNT' && !isUuid(value)) {
      return 'Введіть ID рахунку у форматі UUID.';
    }
    if (this.manualDestinationType === 'CARD' && !/^\d{16}$/.test(value)) {
      return 'Номер картки має містити 16 цифр.';
    }
    return null;
  }

  close(): void {
    this.onDialogVisibleChange(false);
  }

  submit(): void {
    this.withdrawForm.markAllAsTouched();
    this.serverError = null;

    if (this.withdrawForm.invalid || this.getDestinationError()) {
      return;
    }

    this.isSubmitting = true;
    const formValue = this.withdrawForm.getRawValue();

    this.chestService
      .initiateWithdrawal(this.chest.id, {
        amount: formValue.amount!,
        destinationAccount: this.destinationValue,
        purpose: formValue.purpose,
      })
      .pipe(
        catchError((error) => {
          const detail = error.error?.detail as string | undefined;
          this.serverError =
            detail ??
            'Не вдалося створити запит. Можливо, вже є активне голосування або змінився баланс.';
          return throwError(() => error);
        }),
        finalize(() => (this.isSubmitting = false))
      )
      .subscribe({
        next: () => {
          this.initiated.emit();
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

function normalizeManualDestination(value: string): string {
  return value.replace(/\s+/g, '').trim();
}

function isUuid(value: string): boolean {
  return /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i.test(
    value
  );
}
