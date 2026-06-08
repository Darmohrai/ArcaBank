import { Component, OnInit, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { SidebarComponent } from "./components/sidebar-components/sidebar/sidebar.component";
import { NotificationService } from "./services/NotificationService";
import { GlobalToastComponent } from './components/global-toast/global-toast.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, SidebarComponent, GlobalToastComponent], // Додано сюди
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent implements OnInit {
  title = 'arcabank-client';
  private notificationService = inject(NotificationService);

  ngOnInit() {
    this.notificationService.connect();
  }
}
