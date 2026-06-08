import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NotificationService } from '../../services/NotificationService';

@Component({
  selector: 'app-notifications',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './notifications.component.html',
  styleUrls: ['./notifications.component.css']
})
export class NotificationsComponent {
  private notificationService = inject(NotificationService);

  // Отримуємо потік сповіщень із сервісу
  public notifications$ = this.notificationService.notifications$;

  // Метод для визначення кольору та іконки залежно від типу сповіщення
  getIconData(type: string): { emoji: string, colorClass: string } {
    const t = type ? type.toUpperCase() : '';
    switch (t) {
      case 'TRANSFER':
      case 'DEPOSIT':
      case 'PAYMENT':
        return { emoji: '💸', colorClass: 'bg-green' };
      case 'SECURITY':
      case 'WARNING':
      case 'ERROR':
        return { emoji: '🛡️', colorClass: 'bg-red' };
      case 'SYSTEM':
      case 'INFO':
        return { emoji: 'ℹ️', colorClass: 'bg-blue' };
      default:
        return { emoji: '🔔', colorClass: 'bg-gray' };
    }
  }

  markAsRead(id: string) {
    this.notificationService.markAsRead(id);
  }

  markAllAsRead() {
    this.notificationService.markAllAsRead();
  }
}
