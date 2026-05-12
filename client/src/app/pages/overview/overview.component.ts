import { Component } from '@angular/core';
import {BalanceElementComponent} from "../../components/overview-components/balance-element/balance-element.component";
import {GoalsElementComponent} from "../../components/overview-components/goals-element/goals-element.component";
import {
  RecentTransactionsComponent
} from "../../components/overview-components/recent-transactions/recent-transactions.component";

@Component({
  selector: 'app-overview',
  standalone: true,
  imports: [
    BalanceElementComponent,
    GoalsElementComponent,
    RecentTransactionsComponent
  ],
  templateUrl: './overview.component.html',
  styleUrl: './overview.component.css'
})
export class OverviewComponent {

}
