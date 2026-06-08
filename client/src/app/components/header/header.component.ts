import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router'; // Обов'язково
import { NotificationService } from '../../services/NotificationService';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css'],
  standalone: true,
  imports: [CommonModule, RouterModule]
})
export class HeaderComponent implements OnInit, OnDestroy {
  unreadCount: number = 0;
  private countSub!: Subscription;

  constructor(private notificationService: NotificationService) {}

  public unreadCount$ = this.notificationService.unreadCount$;

  ngOnInit(): void {
    this.notificationService.connect();

    this.countSub = this.notificationService.unreadCount$.subscribe(count => {
      this.unreadCount = count;
    });
  }

  ngOnDestroy(): void {
    if (this.countSub) this.countSub.unsubscribe();
  }
}
