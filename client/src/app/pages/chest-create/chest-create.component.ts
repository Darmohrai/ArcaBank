import { Component, inject } from '@angular/core';
import {
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { NgForOf, NgIf } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { ToastModule } from 'primeng/toast';
import { MessageService } from 'primeng/api';
import { catchError, finalize, throwError } from 'rxjs';

import { ChestService } from '../../services/ChestService';
import { ChestCreationRequest } from '../../request/ChestRequest';

@Component({
  selector: 'app-chest-create',
  standalone: true,
  imports: [ReactiveFormsModule, NgForOf, NgIf, ToastModule, RouterLink],
  providers: [MessageService],
  templateUrl: './chest-create.component.html',
  styleUrl: './chest-create.component.css',
})
export class ChestCreateComponent {
  private chestService = inject(ChestService);
  private messageService = inject(MessageService);
  private router = inject(Router);

  private readonly phonePattern = /^\+380\d{9}$/;

  members: string[] = [];
  invalidPhones = new Set<string>();
  memberPhoneInput = new FormControl('', { nonNullable: true });
  memberPhoneError: string | null = null;

  isSubmitting = false;
  serverError: string | null = null;

  createForm = new FormGroup({
    name: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.maxLength(255)],
    }),
    targetAmount: new FormControl<number | null>(null, {
      validators: [Validators.required, Validators.min(0.01)],
    }),
    description: new FormControl(''),
    pin: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.pattern(/^\d{4}$/)],
    }),
    currency: new FormControl<'UAH' | 'USD' | 'EUR'>('UAH', { nonNullable: true }),
  });

  addMember(): void {
    this.memberPhoneError = null;
    const phone = this.memberPhoneInput.value.trim();

    if (!phone) {
      this.memberPhoneError = 'Введіть номер телефону.';
      return;
    }
    if (!this.phonePattern.test(phone)) {
      this.memberPhoneError = 'Формат: +380XXXXXXXXX';
      return;
    }
    if (this.members.includes(phone)) {
      this.memberPhoneError = 'Цей номер уже додано.';
      return;
    }

    this.members.push(phone);
    this.invalidPhones.delete(phone);
    this.memberPhoneInput.setValue('');
  }

  onMemberKeydown(event: KeyboardEvent): void {
    if (event.key === 'Enter') {
      event.preventDefault();
      this.addMember();
    }
  }

  removeMember(phone: string): void {
    this.members = this.members.filter((m) => m !== phone);
    this.invalidPhones.delete(phone);
  }

  submit(): void {
    this.createForm.markAllAsTouched();
    this.serverError = null;
    this.invalidPhones.clear();
    this.memberPhoneError = null;

    if (this.createForm.invalid) {
      return;
    }
    if (this.members.length === 0) {
      this.memberPhoneError = 'Додайте хоча б одного довіреного учасника.';
      return;
    }

    this.isSubmitting = true;

    const formValue = this.createForm.getRawValue();
    const payload: ChestCreationRequest = {
      name: formValue.name,
      targetAmount: formValue.targetAmount!,
      description: formValue.description || undefined,
      friendPhones: this.members,
      pin: formValue.pin,
      currency: formValue.currency,
    };

    this.chestService
      .createChest(payload)
      .pipe(
        catchError((error) => {
          const title = error.error?.title as string | undefined;
          const detail = error.error?.detail as string | undefined;

          if (title === 'USER_NOT_FOUND') {
            const match = detail?.match(/\+380\d{9}/);
            if (match) {
              this.invalidPhones.add(match[0]);
            }
            this.serverError = 'Користувача з таким номером не знайдено.';
          } else if (title === 'VALIDATION_ERROR') {
            this.serverError = detail ?? 'Перевірте правильність даних.';
          } else {
            this.serverError = detail ?? 'Сталася помилка. Спробуйте пізніше.';
          }
          return throwError(() => error);
        }),
        finalize(() => (this.isSubmitting = false))
      )
      .subscribe({
        next: (response) => {
          this.messageService.add({
            severity: 'success',
            summary: 'Успіх!',
            detail: 'Скриню успішно створено.',
          });
          this.router.navigate(['/chests', response.id]);
        },
      });
  }

  getTargetAmountError(): string | null {
    const ctrl = this.createForm.controls.targetAmount;
    if (!ctrl.touched || !ctrl.errors) return null;
    if (ctrl.errors['required']) return 'Введіть цільову суму.';
    if (ctrl.errors['min']) return 'Сума повинна бути більше 0.';
    return null;
  }

  getPinError(): string | null {
    const ctrl = this.createForm.controls.pin;
    if (!ctrl.touched || !ctrl.errors) return null;
    if (ctrl.errors['required']) return 'Введіть PIN-код.';
    if (ctrl.errors['pattern']) return 'PIN-код має містити 4 цифри.';
    return null;
  }

  get isSubmitDisabled(): boolean {
    return this.isSubmitting || this.createForm.invalid || this.members.length === 0;
  }
}
