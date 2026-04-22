import { Component } from '@angular/core';
import {NgIf} from "@angular/common";

@Component({
  selector: 'app-sidebar-logout-btn',
  standalone: true,
    imports: [
        NgIf
    ],
  templateUrl: './sidebar-logout-btn.component.html',
  styleUrl: './sidebar-logout-btn.component.css'
})
export class SidebarLogoutBtnComponent {

}
