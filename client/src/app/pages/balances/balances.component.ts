import {Component, inject, OnInit} from '@angular/core';
import {AccountService} from "../../services/AccountService";
import {CardService} from "../../services/CardService";
import {DialogModule} from "primeng/dialog";
import {FormControl, FormGroup, ReactiveFormsModule, Validators} from "@angular/forms";
import {CreateAccountRequest} from "../../request/AccountRequest";
import {ToastModule} from "primeng/toast";
import {MessageService} from "primeng/api";
import {TabViewModule} from "primeng/tabview";
import {NgForOf, NgIf} from "@angular/common";
import {CreateAccountResponse} from "../../response/AccountResponse";
import {getAllCardsResponse} from "../../response/CardsResponse";

interface AccountTab {
  account: CreateAccountResponse;
  cards: getAllCardsResponse[];
}

@Component({
  selector: 'app-balances',
  standalone: true,
  imports: [
    DialogModule,
    ReactiveFormsModule,
    ToastModule,
    TabViewModule,
    NgForOf,
    NgIf
  ],
  providers: [MessageService],
  templateUrl: './balances.component.html',
  styleUrl: './balances.component.css'
})
export class BalancesComponent implements OnInit {

  private accountService = inject(AccountService);
  private cardService = inject(CardService);
  private messageService = inject(MessageService);

  visibleAccountCreation = false;
  visibleCardCreation = false;

  accountTabs: AccountTab[] = [];
  activeTabIndex = 0;

  createAccountForm = new FormGroup({
    currency: new FormControl<'UAH' | 'USD' | 'EUR'>('UAH', {nonNullable: true}),
    type: new FormControl<'DEBIT' | 'CREDIT' | 'VIRTUAL' | 'CHECKING' | 'SAVINGS'>('DEBIT', {nonNullable: true}),
  });

  createCardForm = new FormGroup({
    pin: new FormControl<string>('', {
      nonNullable: true,
      validators: [
        Validators.required,
        Validators.pattern(/^\d{4}$/)
      ]
    })
  });

  ngOnInit(): void {
    this.loadAccountsWithCards();
  }

  loadAccountsWithCards(): void {
    this.accountService.getAllAccounts().subscribe({
      next: (accounts) => {
        this.accountTabs = accounts.map(account => ({
          account,
          cards: []
        }));
        this.loadAllCards();
      },
      error: () => this.showError('Помилка!', 'Не вдалося завантажити рахунки.')
    });
  }

  loadAllCards(): void {
    this.cardService.getAllCards().subscribe({
      next: (allCards) => {
        this.accountTabs = this.accountTabs.map(tab => ({
          ...tab,
          cards: allCards.filter(card => card.accountId === tab.account.id)
        }));
      },
      error: () => this.showError('Помилка!', 'Не вдалося завантажити картки.')
    });
  }

  get activeAccountId(): string | null {
    return this.accountTabs[this.activeTabIndex]?.account?.id ?? null;
  }

  submitAccountCreationForm(): void {
    const payload: CreateAccountRequest = {
      currency: this.createAccountForm.controls.currency.value,
      type: this.createAccountForm.controls.type.value
    };

    this.accountService.createAccount(payload).subscribe({
      next: () => {
        this.showSuccess('Успіх!', 'Рахунок створено.');
        this.visibleAccountCreation = false;
        this.loadAccountsWithCards();
      },
      error: () => this.showError('Помилка!', 'Не вдалося відкрити рахунок. Спробуйте пізніше.')
    });
  }

  submitCardCreationForm(): void {
    if (this.createCardForm.invalid || !this.activeAccountId) return;

    const payload = {pin: Number(this.createCardForm.controls.pin.value)};

    this.cardService.createCardByAccount(payload, this.activeAccountId).subscribe({
      next: () => {
        this.showSuccess('Успіх!', 'Картку створено.');
        this.visibleCardCreation = false;
        this.createCardForm.reset();
        this.loadAllCards();
      },
      error: () => this.showError('Помилка!', 'Не вдалося створити картку. Спробуйте пізніше.')
    });
  }

  showAccountCreationDialog(): void {
    this.visibleAccountCreation = true;
  }

  showCardCreationDialog(): void {
    this.visibleCardCreation = true;
  }

  showSuccess(summary: string, detail: string): void {
    this.messageService.add({severity: 'success', summary, detail});
  }

  showError(summary: string, detail: string): void {
    this.messageService.add({severity: 'error', summary, detail});
  }

  formatCardNumber(cardNumber: string): string {
    return cardNumber.replace(/(.{4})/g, '$1 ').trim();
  }
}
