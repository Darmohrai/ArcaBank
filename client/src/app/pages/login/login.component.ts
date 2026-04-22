import {Component, inject} from '@angular/core';
import {EmailInputComponent} from "../../components/input-components/email-input/email-input.component";
import {PasswordInputComponent} from "../../components/input-components/password-input/password-input.component";
import {CheckboxComponent} from "../../components/button-components/checkbox/checkbox.component";
import {PrimaryBtnComponent} from "../../components/button-components/primary-btn/primary-btn.component";
import {Router} from "@angular/router";

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    EmailInputComponent,
    PasswordInputComponent,
    CheckboxComponent,
    PrimaryBtnComponent
  ],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {

  private router = inject(Router);

  goToSignUp() {
    this.router.navigateByUrl('/signup');
  }

  goToForgotPassword() {
    this.router.navigateByUrl('/forgot-password');
  }

}
