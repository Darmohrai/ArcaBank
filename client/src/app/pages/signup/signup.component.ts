import {Component, inject} from '@angular/core';
import {CheckboxComponent} from "../../components/button-components/checkbox/checkbox.component";
import {EmailInputComponent} from "../../components/input-components/email-input/email-input.component";
import {PasswordInputComponent} from "../../components/input-components/password-input/password-input.component";
import {PrimaryBtnComponent} from "../../components/button-components/primary-btn/primary-btn.component";
import {RegularInputComponent} from "../../components/input-components/regular-input/regular-input.component";
import {Router} from "@angular/router";

@Component({
  selector: 'app-signup',
  standalone: true,
  imports: [
    CheckboxComponent,
    EmailInputComponent,
    PasswordInputComponent,
    PrimaryBtnComponent,
    RegularInputComponent
  ],
  templateUrl: './signup.component.html',
  styleUrl: './signup.component.css'
})
export class SignupComponent {

  private router = inject(Router)

  goToLogin() {
    this.router.navigateByUrl('/login');
  }

}
