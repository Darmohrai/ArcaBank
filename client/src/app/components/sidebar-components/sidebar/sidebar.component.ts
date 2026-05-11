import {Component, inject} from '@angular/core';
import {SidebarBtnComponent} from "../sidebar-btn/sidebar-btn.component";
import {SidebarLogoutBtnComponent} from "../sidebar-logout-btn/sidebar-logout-btn.component";
import {SidebarUserProfileComponent} from "../sidebar-user-profile/sidebar-user-profile.component";
import {AuthService} from "../../../services/AuthService";
import {Router} from "@angular/router";

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [
    SidebarBtnComponent,
    SidebarLogoutBtnComponent,
    SidebarUserProfileComponent
  ],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.css'
})
export class SidebarComponent {

  protected auth = inject(AuthService);
  private router = inject(Router);


  navigateByUrl(url: string) {
    this.router.navigateByUrl(url);
  }

}
