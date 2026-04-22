import { Component } from '@angular/core';
import {PasswordModule} from "primeng/password";
import {ReactiveFormsModule} from "@angular/forms";

@Component({
  selector: 'app-password-input',
  standalone: true,
  imports: [
    PasswordModule,
    ReactiveFormsModule
  ],
  templateUrl: './password-input.component.html',
  styleUrl: './password-input.component.css'
})
export class PasswordInputComponent {

}
