import { Injectable, NgZone, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Client, Message } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { BehaviorSubject, Subject } from 'rxjs';

export interface AppNotification {
  id: string;
  title: string;
  notificationType: string;
  text: string;
  isRead: boolean;
}

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private http = inject(HttpClient);
  private ngZone = inject(NgZone);
  private apiUrl = 'http://localhost:8082/api/v1/notifications';
  private stompClient: Client;

  private notificationsSubject = new BehaviorSubject<AppNotification[]>([]);
  public notifications$ = this.notificationsSubject.asObservable();

  private unreadCountSubject = new BehaviorSubject<number>(0);
  public unreadCount$ = this.unreadCountSubject.asObservable();

  public newNotification$ = new Subject<AppNotification>();

  constructor() {
    this.stompClient = new Client({
      webSocketFactory: () => new SockJS('http://localhost:8082/ws-notifications'),
      debug: (str) => { console.log('STOMP: ' + str); },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });
  }

  // 1. ПІДКЛЮЧЕННЯ (Тепер метод є!)
  public connect() {
    if (this.stompClient.active) return;

    this.loadHistory(); // Завантажуємо історію з БД

    this.stompClient.onConnect = () => {
      const userId = this.getCurrentUserId();
      if (!userId) return;

      this.stompClient.subscribe(`/topic/user.${userId}`, (message: Message) => {
        if (message.body) {
          const newNotif = JSON.parse(message.body);
          this.ngZone.run(() => {
            const current = this.notificationsSubject.value;
            this.notificationsSubject.next([newNotif, ...current]);
            this.unreadCountSubject.next(this.unreadCountSubject.value + 1);
            this.newNotification$.next(newNotif);
          });
        }
      });
    };
    this.stompClient.activate();
  }

  // 2. ЗАВАНТАЖЕННЯ ІСТОРІЇ
  private loadHistory() {
    const userId = this.getCurrentUserId();
    if (!userId) return;

    this.http.get<AppNotification[]>(`${this.apiUrl}/history/${userId}`).subscribe({
      next: (history) => {
        this.notificationsSubject.next(history);
        this.unreadCountSubject.next(history.filter(n => !n.isRead).length);
      }
    });
  }

  // 3. ПОЗНАЧИТИ ЯК ПРОЧИТАНЕ
  public markAsRead(id: string) {
    this.http.patch(`${this.apiUrl}/${id}/read`, {}).subscribe(() => {
      const current = this.notificationsSubject.value.map(n =>
        n.id === id ? { ...n, isRead: true } : n
      );
      this.notificationsSubject.next(current);
      this.unreadCountSubject.next(Math.max(0, this.unreadCountSubject.value - 1));
    });
  }

  // 4. ПОЗНАЧИТИ ВСІ ЯК ПРОЧИТАНІ
  public markAllAsRead() {
    const userId = this.getCurrentUserId();
    if (!userId) return;

    this.http.patch(`${this.apiUrl}/read-all/${userId}`, {}).subscribe(() => {
      const current = this.notificationsSubject.value.map(n => ({ ...n, isRead: true }));
      this.notificationsSubject.next(current);
      this.unreadCountSubject.next(0);
    });
  }

  public disconnect() {
    if (this.stompClient.active) {
      this.stompClient.deactivate();
    }
  }

  private getCurrentUserId(): string | null {
    const token = localStorage.getItem('auth_token') || sessionStorage.getItem('auth_token');
    if (!token) return null;
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.sub;
    } catch { return null; }
  }
}
