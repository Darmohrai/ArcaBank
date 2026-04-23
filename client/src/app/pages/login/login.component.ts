import {Component, inject} from '@angular/core';
import {EmailInputComponent} from "../../components/input-components/email-input/email-input.component";
import {PasswordInputComponent} from "../../components/input-components/password-input/password-input.component";
import {CheckboxComponent} from "../../components/button-components/checkbox/checkbox.component";
import {PrimaryBtnComponent} from "../../components/button-components/primary-btn/primary-btn.component";
import {Router} from "@angular/router";
import {FormBuilder, ReactiveFormsModule, Validators} from '@angular/forms';
import {AuthService} from '../../services/AuthService';
import {NgIf} from '@angular/common';
import {finalize} from 'rxjs';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    EmailInputComponent,
    PasswordInputComponent,
    CheckboxComponent,
    PrimaryBtnComponent,
    ReactiveFormsModule,
    NgIf,
  ],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {

  private router = inject(Router);
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);

  isSubmitting = false;
  submitError: string | null = null;

  form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required]],
    rememberMe: [false],
  });

  goToSignUp() {
    this.router.navigateByUrl('/signup');
  }

  goToForgotPassword() {
    this.router.navigateByUrl('/forgot-password');
  }

  onSubmit() {
    this.submitError = null;

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const {email, password, rememberMe} = this.form.getRawValue();
    this.isSubmitting = true;
    this.auth
      .signIn({email, password}, rememberMe)
      .pipe(finalize(() => (this.isSubmitting = false)))
      .subscribe({
        next: () => this.router.navigateByUrl('/'),
        error: (err) => {
          this.submitError =
            err?.error?.message ||
            err?.message ||
            'Не вдалося увійти. Перевірте email/пароль і спробуйте ще раз.';
        },
      });
  }

}
