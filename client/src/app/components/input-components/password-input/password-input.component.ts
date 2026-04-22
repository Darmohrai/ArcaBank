import { Component } from '@angular/core';
import {PasswordModule} from "primeng/password";
import {FormsModule} from "@angular/forms";

@Component({
  selector: 'app-password-input',
  standalone: true,
  imports: [
    PasswordModule,
    FormsModule
  ],
  templateUrl: './password-input.component.html',
  styleUrl: './password-input.component.css'
})
export class PasswordInputComponent {
  value!: string;
}
