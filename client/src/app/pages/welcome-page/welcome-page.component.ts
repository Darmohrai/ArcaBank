import {Component, inject} from '@angular/core';
import {
  PrimaryBtnSmallComponent
} from "../../components/button-components/primary-btn-small/primary-btn-small.component";
import { Router } from '@angular/router';

@Component({
  selector: 'app-welcome-page',
  standalone: true,
  imports: [
    PrimaryBtnSmallComponent
  ],
  templateUrl: './welcome-page.component.html',
  styleUrl: './welcome-page.component.css'
})
export class WelcomePageComponent {

  private router = inject(Router);

  authorisationRedirect() {
    this.router.navigateByUrl('/login');
  }
}
