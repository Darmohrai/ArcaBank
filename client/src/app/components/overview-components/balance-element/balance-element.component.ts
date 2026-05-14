import { Component, inject, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { forkJoin } from 'rxjs';
import { NgFor, NgIf, CurrencyPipe } from '@angular/common';
import {AccountService} from "../../../services/AccountService";
import {CardService} from "../../../services/CardService";
import {CreateAccountResponse} from "../../../response/AccountResponse";
import {getAllCardsResponse} from "../../../response/CardsResponse";



export interface OverviewCardSlide {
  cardHolderName: string;
  maskedNumber: string;
  expirationDate: string;
  status: string;
  currency: string;
  balance: number;
}

@Component({
  selector: 'app-balance-element',
  standalone: true,
  imports: [NgIf, CurrencyPipe],
  templateUrl: './balance-element.component.html',
  styleUrl: './balance-element.component.css',
})
export class BalanceElementComponent implements OnInit {
  private router = inject(Router);
  private accountService = inject(AccountService);
  private cardService = inject(CardService);

  balanceSummary = '—';
  slides: OverviewCardSlide[] = [];
  isLoading = true;

  currentIndex = 0;

  ngOnInit(): void {
    forkJoin({
      accounts: this.accountService.getAllAccounts(),
      cards: this.cardService.getAllCards(),
    }).subscribe({
      next: ({ accounts, cards }) => {
        this.balanceSummary = this.buildBalanceSummary(accounts);
        this.slides = this.buildSlides(cards, accounts);
        this.currentIndex = 0;
        this.isLoading = false;
      },
      error: () => {
        this.balanceSummary = '—';
        this.slides = [];
        this.isLoading = false;
      },
    });
  }

  redirectToAccounts(): void {
    this.router.navigateByUrl('/balances');
  }

  private buildBalanceSummary(accounts: CreateAccountResponse[]): string {
    if (!accounts.length) return '0';
    const byCurrency = new Map<string, number>();
    for (const a of accounts) {
      byCurrency.set(
        a.currency,
        (byCurrency.get(a.currency) ?? 0) + a.balance
      );
    }
    return Array.from(byCurrency.entries())
      .map(([currency, sum]) => {
        const formatted = sum.toLocaleString('uk-UA', {
          minimumFractionDigits: 2,
          maximumFractionDigits: 2,
        });
        return `${formatted} ${currency}`;
      })
      .join(' · ');
  }

  private buildSlides(
    cards: getAllCardsResponse[],
    accounts: CreateAccountResponse[]
  ): OverviewCardSlide[] {
    const byAccountId = new Map(accounts.map((a) => [a.id, a]));
    return cards.map((card) => {
      const acc = byAccountId.get(card.accountId);
      return {
        cardHolderName: card.cardHolderName || 'Картка',
        maskedNumber: this.maskCardNumber(card.cardNumber),
        expirationDate: card.expirationDate,
        status: card.status,
        currency: acc?.currency ?? '—',
        balance: acc?.balance ?? 0,
      };
    });
  }

  private maskCardNumber(raw: string): string {
    const digits = raw.replace(/\D/g, '');
    if (digits.length < 4) return '****';
    const last4 = digits.slice(-4);
    return `**** **** **** ${last4}`;
  }

  next(): void {
    if (this.currentIndex < this.slides.length - 1) {
      this.currentIndex++;
    }
  }

  prev(): void {
    if (this.currentIndex > 0) {
      this.currentIndex--;
    }
  }

  goTo(index: number): void {
    this.currentIndex = index;
  }
}
