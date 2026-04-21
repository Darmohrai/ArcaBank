import { Component } from '@angular/core';
import {PrimaryBtnComponent} from "../../components/button-components/primary-btn/primary-btn.component";
import {SecondaryBtnComponent} from "../../components/button-components/secondary-btn/secondary-btn.component";
import {
  PrimaryBtnSmallComponent
} from "../../components/button-components/primary-btn-small/primary-btn-small.component";
import {CheckboxComponent} from "../../components/button-components/checkbox/checkbox.component";

@Component({
  selector: 'app-welcome-page',
  standalone: true,
  imports: [
    PrimaryBtnComponent,
    SecondaryBtnComponent,
    PrimaryBtnSmallComponent,
    CheckboxComponent
  ],
  templateUrl: './welcome-page.component.html',
  styleUrl: './welcome-page.component.css'
})
export class WelcomePageComponent {

}
