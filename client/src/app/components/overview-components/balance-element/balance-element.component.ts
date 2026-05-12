import {Component, inject, Input} from '@angular/core';
import { Router } from "@angular/router";

@Component({
  selector: 'app-balance-element',
  standalone: true,
  imports: [],
  templateUrl: './balance-element.component.html',
  styleUrl: './balance-element.component.css'
})
export class BalanceElementComponent {
  @Input() currentTotalBalance: number = 0;
  private router = inject(Router)

  redirectToAccounts() {
    this.router.navigateByUrl('/balances');
  }

  currentIndex = 0;

  cards = [
    {
      type: 'Credit Card',
      number: '**** **** **** 2598',
      balance: '$25000'
    },
    {
      type: 'Debit Card',
      number: '**** **** **** 1254',
      balance: '$12000'
    },
    {
      type: 'Virtual Card',
      number: '**** **** **** 8831',
      balance: '$4000'
    },
  ];

  next() {
    if (this.currentIndex < this.cards.length - 1) {
      this.currentIndex++;
    }
  }

  prev() {
    if (this.currentIndex > 0) {
      this.currentIndex--;
    }
  }

  goTo(index: number) {
    this.currentIndex = index;
  }
}
