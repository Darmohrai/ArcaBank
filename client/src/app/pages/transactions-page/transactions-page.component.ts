import { Component, inject, OnInit } from '@angular/core';
import { NgForOf, NgIf, DatePipe, CurrencyPipe } from '@angular/common';
import { forkJoin } from 'rxjs';

import { TransfersService } from '../../services/TransfersService';
import { AccountService } from '../../services/AccountService';
import { Transaction } from '../../response/TransferResponse';
import {
  classifyTransaction,
  humanizeTransactionType,
  shortId as shortAccountId,
  transactionSubtitle,
  TransactionDirection,
} from '../../util/transaction-helpers';

interface TransactionRow {
  raw: Transaction;
  direction: TransactionDirection;
  title: string;
  subtitle: string;
}

@Component({
  selector: 'app-transactions-page',
  standalone: true,
  imports: [NgForOf, NgIf, DatePipe, CurrencyPipe],
  templateUrl: './transactions-page.component.html',
  styleUrl: './transactions-page.component.css',
})
export class TransactionsPageComponent implements OnInit {
  private transfersService = inject(TransfersService);
  private accountService = inject(AccountService);

  rows: TransactionRow[] = [];
  isLoading = true;
  loadError = false;

  page = 0;
  readonly pageSize = 15;
  totalPages = 0;
  totalElements = 0;

  private myAccountIds = new Set<string>();

  readonly skeletonSlots = [0, 1, 2, 3, 4, 5];

  ngOnInit(): void {
    forkJoin({
      accounts: this.accountService.getAllAccounts(),
      history: this.transfersService.getUserTransactionHistory(this.page, this.pageSize),
    }).subscribe({
      next: ({ accounts, history }) => {
        this.myAccountIds = new Set(accounts.map((a) => a.id));
        this.applyHistory(history);
        this.isLoading = false;
      },
      error: () => {
        this.loadError = true;
        this.isLoading = false;
      },
    });
  }

  private applyHistory(history: {
    content: Transaction[];
    totalPages: number;
    totalElements: number;
  }): void {
    this.totalPages = history.totalPages;
    this.totalElements = history.totalElements;
    const sorted = [...history.content].sort(
      (a, b) =>
        new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
    );
    this.rows = sorted.map((t) => ({
      raw: t,
      direction: classifyTransaction(t, this.myAccountIds),
      title: humanizeTransactionType(t.type),
      subtitle: transactionSubtitle(t, this.myAccountIds),
    }));
  }

  loadPage(delta: number): void {
    const next = this.page + delta;
    if (next < 0 || next >= this.totalPages) return;
    this.page = next;
    this.isLoading = true;
    this.transfersService
      .getUserTransactionHistory(this.page, this.pageSize)
      .subscribe({
        next: (history) => {
          this.applyHistory(history);
          this.isLoading = false;
        },
        error: () => {
          this.loadError = true;
          this.isLoading = false;
        },
      });
  }

  truncateId(id: string): string {
    return shortAccountId(id);
  }
}
