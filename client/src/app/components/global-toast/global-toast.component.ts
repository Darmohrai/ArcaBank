import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NotificationService, AppNotification } from '../../services/NotificationService';

@Component({
  selector: 'app-global-toast',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="toast-container">
      <div class="toast" *ngFor="let toast of toasts" (click)="remove(toast)">
        <div class="toast-icon">{{ getIcon(toast.notificationType) }}</div>
        <div class="toast-content">
          <strong>{{ toast.title }}</strong>
          <p>{{ toast.text }}</p>
        </div>
        <button class="close-btn" (click)="remove(toast); $event.stopPropagation()">&times;</button>
      </div>
    </div>
  `,
  styles: [`
    .toast-container {
      position: fixed;
      bottom: 24px;
      right: 24px;
      z-index: 9999;
      display: flex;
      flex-direction: column;
      gap: 12px;
    }
    .toast {
      background: #ffffff;
      border-left: 4px solid #007bff;
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
      border-radius: 12px;
      padding: 16px;
      display: flex;
      align-items: center;
      gap: 12px;
      width: 320px;
      cursor: pointer;
      animation: slideIn 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
      transition: opacity 0.3s ease;
    }
    .toast:hover { box-shadow: 0 10px 28px rgba(0, 0, 0, 0.15); }
    .toast-icon { font-size: 1.5rem; }
    .toast-content { flex-grow: 1; overflow: hidden; }
    .toast-content strong { display: block; font-size: 0.95rem; color: #1a1a1a; margin-bottom: 4px; }
    .toast-content p { display: block; font-size: 0.85rem; color: #666; margin: 0; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
    .close-btn { background: none; border: none; font-size: 1.5rem; color: #999; cursor: pointer; padding: 0; margin-left: 4px; line-height: 1; }
    .close-btn:hover { color: #333; }
    @keyframes slideIn {
      from { transform: translateX(120%); opacity: 0; }
      to { transform: translateX(0); opacity: 1; }
    }
  `]
})
export class GlobalToastComponent implements OnInit {
  private notificationService = inject(NotificationService);
  toasts: AppNotification[] = [];

  ngOnInit() {
    this.notificationService.newNotification$.subscribe(notif => {
      this.toasts.push(notif);
      setTimeout(() => this.remove(notif), 6000);
    });
  }

  remove(notif: AppNotification) {
    this.toasts = this.toasts.filter(t => t !== notif);
  }

  getIcon(type: string): string {
    const t = type ? type.toUpperCase() : '';
    if (['TRANSFER', 'DEPOSIT', 'PAYMENT'].includes(t)) return '💸';
    if (['SECURITY', 'WARNING', 'ERROR'].includes(t)) return '🛡️';
    return '🔔';
  }
}
