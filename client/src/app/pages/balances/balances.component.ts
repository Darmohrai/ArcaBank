import {Component, inject, OnInit} from '@angular/core';
import {AccountService} from "../../services/AccountService";
import {DialogModule} from "primeng/dialog";
import {FormControl, FormGroup, ReactiveFormsModule} from "@angular/forms";
import {CreateAccountRequest} from "../../request/AccountRequest";
import {ToastModule} from "primeng/toast";
import {MessageService} from "primeng/api";
import {TabViewModule} from "primeng/tabview";
import {tabs} from "../../models/BalancesTabsInterface";
import {NgForOf} from "@angular/common";

@Component({
  selector: 'app-balances',
  standalone: true,
  imports: [
    DialogModule,
    ReactiveFormsModule,
    ToastModule,
    TabViewModule,
    NgForOf
  ],
  providers: [MessageService],
  templateUrl: './balances.component.html',
  styleUrl: './balances.component.css'
})
export class BalancesComponent implements OnInit {


  private AccountService = inject(AccountService)
  private messageService = inject(MessageService)
  visibleAccountCreation: boolean = false;
  visibleCardCreation: boolean = false;
  testTabs: tabs[] = []

  createAccountForm = new FormGroup({
    currency: new FormControl<'UAH' | 'USD' | 'EUR'>('UAH', {
      nonNullable: true
    }),
    type: new FormControl<'DEBIT' | 'CREDIT' | 'VIRTUAL' | 'CHECKING' | 'SAVINGS'>('DEBIT', {
      nonNullable: true
    }),
  })

  createCardForm = new FormGroup({})

  submitAccountCreationForm() {

    const payloadData: CreateAccountRequest = {
      currency: this.createAccountForm.controls.currency.value,
      type: this.createAccountForm.controls.type.value
    }

    this.AccountService.createAccount(payloadData)
      .subscribe({
        next: (response) => {
          console.log('SUCCESS', response);
          this.showSuccess('Успіх!', 'Рахунок створено.')
          this.visibleAccountCreation = false;
        },
        error: (error) => {
          this.showError('Помилка!', 'Не вдалося відкрити рахунок. Спробуйте пізніше.')
          console.log('ERROR', error);
        }
      })
  }

  submitCardCreationForm() {
    console.log('ok')
  }


  showAccountCreationDialog() {
    this.visibleAccountCreation = true;
  }

  showCardCreationDialog() {
    this.visibleCardCreation = true;
  }

  showSuccess(summary: string, detail: string) {
    this.messageService.add({ severity: 'success', summary: summary, detail: detail });
  }

  showError(summary: string, detail: string) {
    this.messageService.add({ severity: 'error', summary: summary, detail: detail });
  }

  ngOnInit(): void {
      this.testTabs = [
        {
          title: 'account 1',
          content: [
            {
              "id": '123',
              "accountId": 'acc123',
              "cardNumber": '1111 5555 4444 2222',
              "cardHolderName": 'Redko Arsenii',
              "expirationDate": '05.10.2027',
              "status": 'ACTIVE'
            },
            {
              "id": '333',
              "accountId": 'acc333',
              "cardNumber": '9999 5555 0000 2222',
              "cardHolderName": 'Redko Arsenii',
              "expirationDate": '05.10.2027',
              "status": 'ACTIVE'
            }
          ]
        },
        {
          title: 'account 2',
          content: [
            {
              id: '8f3a21c4-91d2-4b7a-9c11-6e2d8f1a4c55',
              accountId: 'acc781',
              cardNumber: '4532 1987 6543 1098',
              cardHolderName: 'Ivan Petrenko',
              expirationDate: '11.09.2028',
              status: 'ACTIVE'
            },
            {
              id: 'c19d5a2e-7b6f-4a88-9f32-2d91c0e7b6aa',
              accountId: 'acc902',
              cardNumber: '5500 1234 9876 3321',
              cardHolderName: 'Oleh Kovalenko',
              expirationDate: '03.06.2029',
              status: 'ACTIVE'
            },
            {
              id: 'c19d5a2e-7b6f-4a88-9f32-2d91c0e7b6aa',
              accountId: 'acc902',
              cardNumber: '5500 1234 9876 3321',
              cardHolderName: 'Oleh Kovalenko',
              expirationDate: '03.06.2029',
              status: 'ACTIVE'
            },
            {
              id: 'c19d5a2e-7b6f-4a88-9f32-2d91c0e7b6aa',
              accountId: 'acc902',
              cardNumber: '5500 1234 9876 3321',
              cardHolderName: 'Oleh Kovalenko',
              expirationDate: '03.06.2029',
              status: 'ACTIVE'
            }
          ]
        }
      ]
  }

}
