import {Component, inject} from '@angular/core';
import {PrimaryBtnComponent} from "../../components/button-components/primary-btn/primary-btn.component";
import {EmailInputComponent} from '../../components/input-components/email-input/email-input.component';
import {PasswordInputComponent} from '../../components/input-components/password-input/password-input.component';
import {RegularInputComponent} from '../../components/input-components/regular-input/regular-input.component';
import {Router} from "@angular/router";
import {FormBuilder, ReactiveFormsModule, Validators} from '@angular/forms';
import {AuthService} from '../../services/AuthService';
import {RegisterRequest} from '../../request/RegisterRequest';
import {NgIf} from '@angular/common';
import {finalize} from 'rxjs';

@Component({
  selector: 'app-signup',
  standalone: true,
  imports: [
    PrimaryBtnComponent,
    RegularInputComponent,
    EmailInputComponent,
    PasswordInputComponent,
    ReactiveFormsModule,
    NgIf,
  ],
  templateUrl: './signup.component.html',
  styleUrl: './signup.component.css'
})
export class SignupComponent {

  private router = inject(Router);
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);

  private readonly phoneUaPattern = /^\+380\d{9}$/;
  private readonly passwordPattern = /^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).{8,}$/;

  isSubmitting = false;
  submitError: string | null = null;

  form = this.fb.nonNullable.group({
    passport_id: ['', [Validators.required, Validators.pattern(/^\d{10}$/)]],
    firstName: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(50)]],
    lastName: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(50)]],
    email: ['', [Validators.required, Validators.email]],
    phoneNumber: ['', [Validators.required, Validators.pattern(this.phoneUaPattern)]],
    password: ['', [Validators.required, Validators.pattern(this.passwordPattern)]],
  });

  goToLogin() {
    this.router.navigateByUrl('/login');
  }

  onSubmit() {
    this.submitError = null;

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const payload = this.form.getRawValue() as RegisterRequest;
    this.isSubmitting = true;
    this.auth
      .register(payload)
      .pipe(finalize(() => (this.isSubmitting = false)))
      .subscribe({
        next: () => this.router.navigateByUrl('/login'),
        error: (err) => {
          this.submitError =
            err?.error?.message ||
            err?.message ||
            'Не вдалося зареєструватися. Перевірте дані і спробуйте ще раз.';
        },
      });
  }

}
