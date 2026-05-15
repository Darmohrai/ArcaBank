import { Component, inject, OnInit } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { NgIf, CurrencyPipe, DatePipe } from '@angular/common';
import { finalize } from 'rxjs';

import { AccountService } from '../../services/AccountService';
import { CreateAccountResponse } from '../../response/AccountResponse';
import {
  exportAccountStatementPdf,
  statementExportErrorMessage,
} from '../../util/statement-export';

@Component({
  selector: 'app-balance-detail',
  standalone: true,
  imports: [NgIf, CurrencyPipe, DatePipe, RouterLink],
  templateUrl: './balance-detail.component.html',
  styleUrl: './balance-detail.component.css',
})
export class BalanceDetailComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private accountService = inject(AccountService);

  account: CreateAccountResponse | null = null;
  isLoading = true;
  notFound = false;
  isExporting = false;
  exportError: string | null = null;

  ngOnInit(): void {
    const accountId = this.route.snapshot.paramMap.get('id');
    if (!accountId) {
      this.notFound = true;
      this.isLoading = false;
      return;
    }

    this.accountService.getAllAccounts().subscribe({
      next: (accounts) => {
        this.account = accounts.find((a) => a.id === accountId) ?? null;
        this.notFound = !this.account;
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
        this.notFound = true;
      },
    });
  }

  downloadStatement(): void {
    if (!this.account) return;
    this.exportError = null;
    this.isExporting = true;

    exportAccountStatementPdf(
      this.accountService,
      this.account.id,
      this.account.currency
    )
      .pipe(finalize(() => (this.isExporting = false)))
      .subscribe({
        next: () => {},
        error: async (error) => {
          this.exportError = await statementExportErrorMessage(error);
        },
      });
  }
}
