import {Component, inject} from '@angular/core';
import {AccountService} from "../../services/AccountService";
import {Button} from "primeng/button";
import {DialogModule} from "primeng/dialog";
import {FormControl, FormGroup, ReactiveFormsModule} from "@angular/forms";
import {CreateAccountRequest} from "../../request/AccountRequest";
import {ToastModule} from "primeng/toast";
import {MessageService} from "primeng/api";

@Component({
  selector: 'app-balances',
  standalone: true,
  imports: [
    Button,
    DialogModule,
    ReactiveFormsModule,
    ToastModule
  ],
  providers: [MessageService],
  templateUrl: './balances.component.html',
  styleUrl: './balances.component.css'
})
export class BalancesComponent {

  constructor(private messageService: MessageService) {}

  private AccountService = inject(AccountService)
  visible: boolean = false;

  createAccountForm = new FormGroup({
    currency: new FormControl<'UAH' | 'USD' | 'EUR'>('UAH', {
      nonNullable: true
    }),
    type: new FormControl<'DEBIT' | 'CREDIT' | 'VIRTUAL'>('DEBIT', {
      nonNullable: true
    }),
  })

  submitForm() {

    const payloadData: CreateAccountRequest = {
      currency: this.createAccountForm.controls.currency.value,
      type: this.createAccountForm.controls.type.value
    }

    this.AccountService.createAccount(payloadData)
      .subscribe({
        next: (response) => {
          console.log('SUCCESS', response);
          this.showSuccess('Успіх!', 'Рахунок створено.')
          this.visible = false;
        },
        error: (error) => {
          this.showError('Помилка!', 'Не вдалося відкрити рахунок. Спробуйте пізніше.')
          console.log('ERROR', error);
        }
      })
  }


  showDialog() {
    this.visible = true;
  }

  showSuccess(summary: string, detail: string) {
    this.messageService.add({ severity: 'success', summary: summary, detail: detail });
  }

  showError(summary: string, detail: string) {
    this.messageService.add({ severity: 'error', summary: summary, detail: detail });
  }


}
