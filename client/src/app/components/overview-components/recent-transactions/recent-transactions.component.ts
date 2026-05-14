import { Component, inject, OnInit } from '@angular/core';
import { NgForOf, NgIf, CurrencyPipe, DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';

import { TransfersService } from '../../../services/TransfersService';
import { AccountService } from '../../../services/AccountService';
import { Transaction } from '../../../response/TransferResponse';
import {
  classifyTransaction,
  humanizeTransactionType,
  transactionSubtitle,
  TransactionDirection,
} from '../../../util/transaction-helpers';

type TabKey = 'all' | 'income' | 'expense';

interface TransactionListItem {
  id: string;
  title: string;
  description: string;
  amount: number;
  currency: string;
  createdAt: string;
  direction: TransactionDirection;
  iconClass: string;
}

@Component({
  selector: 'app-recent-transactions',
  standalone: true,
  imports: [NgForOf, NgIf, CurrencyPipe, DatePipe, RouterLink],
  templateUrl: './recent-transactions.component.html',
  styleUrl: './recent-transactions.component.css',
})
export class RecentTransactionsComponent implements OnInit {
  private transfersService = inject(TransfersService);
  private accountService = inject(AccountService);

  private allSorted: TransactionListItem[] = [];
  private myAccountIds = new Set<string>();

  activeTab: TabKey = 'all';
  currentData: TransactionListItem[] = [];
  isLoading = true;
  loadError = false;

  ngOnInit(): void {
    forkJoin({
      accounts: this.accountService.getAllAccounts(),
      history: this.transfersService.getUserTransactionHistory(0, 40),
    }).subscribe({
      next: ({ accounts, history }) => {
        this.myAccountIds = new Set(accounts.map((a) => a.id));
        const sorted = [...history.content].sort(
          (a, b) =>
            new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
        );
        this.allSorted = sorted.map((t) => this.mapToItem(t));
        this.setTab(this.activeTab);
        this.isLoading = false;
      },
      error: () => {
        this.loadError = true;
        this.isLoading = false;
        this.currentData = [];
      },
    });
  }

  private mapToItem(t: Transaction): TransactionListItem {
    const direction = classifyTransaction(t, this.myAccountIds);
    return {
      id: t.id,
      title: humanizeTransactionType(t.type),
      description: transactionSubtitle(t, this.myAccountIds),
      amount: t.amount,
      currency: t.currency,
      createdAt: t.createdAt,
      direction,
      iconClass: this.iconForType(t.type),
    };
  }

  private iconForType(type: string): string {
    const u = (type ?? '').toUpperCase();
    if (u.includes('DEPOSIT')) return 'pi pi-plus-circle';
    if (u.includes('EXCHANGE')) return 'pi pi-sync';
    if (u.includes('WITHDRAW')) return 'pi pi-minus-circle';
    return 'pi pi-arrow-right-arrow-left';
  }

  setTab(tab: TabKey): void {
    this.activeTab = tab;
    const filtered =
      tab === 'all'
        ? this.allSorted
        : tab === 'income'
          ? this.allSorted.filter((x) => x.direction === 'income')
          : this.allSorted.filter((x) => x.direction === 'expense');
    this.currentData = filtered.slice(0, 5);
  }
}
