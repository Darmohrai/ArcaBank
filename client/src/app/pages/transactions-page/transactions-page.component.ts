import { Component, inject, OnInit } from '@angular/core';
import { NgForOf, NgIf, DatePipe, CurrencyPipe } from '@angular/common';
import { forkJoin } from 'rxjs';
import { finalize } from 'rxjs';

import { TransfersService } from '../../services/TransfersService';
import { AccountService } from '../../services/AccountService';
import { CreateAccountResponse } from '../../response/AccountResponse';
import { Transaction } from '../../response/TransferResponse';
import {
  exportAccountStatementPdf,
  statementExportErrorMessage,
} from '../../util/statement-export';
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

  accounts: CreateAccountResponse[] = [];
  selectedAccountId = '';
  rows: TransactionRow[] = [];
  isLoading = true;
  loadError = false;
  isExporting = false;
  exportError: string | null = null;

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
        this.accounts = accounts;
        if (!this.selectedAccountId && accounts.length > 0) {
          this.selectedAccountId = accounts[0].id;
        }
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

  get selectedAccount(): CreateAccountResponse | undefined {
    return this.accounts.find((a) => a.id === this.selectedAccountId);
  }

  downloadStatement(): void {
    this.exportError = null;
    if (!this.selectedAccountId) {
      this.exportError = 'Оберіть рахунок для виписки.';
      return;
    }

    this.isExporting = true;
    const currency = this.selectedAccount?.currency ?? 'account';

    exportAccountStatementPdf(
      this.accountService,
      this.selectedAccountId,
      currency
    )
      .pipe(finalize(() => (this.isExporting = false)))
      .subscribe({
        next: () => {},
        error: async (error) => {
          this.exportError = await statementExportErrorMessage(error.detail);
        },
      });
  }
}
