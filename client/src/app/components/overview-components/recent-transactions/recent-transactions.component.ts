import {Component} from '@angular/core';
import {NgForOf} from "@angular/common";
import {TabViewModule} from "primeng/tabview";

interface ITransactionElement {
  name: string,
  description: string,
  amount: number,
  date: string
}

type Tabs = 'all' | 'income' | 'expense';

@Component({
  selector: 'app-recent-transactions',
  standalone: true,
  imports: [
    NgForOf,
    TabViewModule
  ],
  templateUrl: './recent-transactions.component.html',
  styleUrl: './recent-transactions.component.css'
})

export class RecentTransactionsComponent {
  data: ITransactionElement[] = [
    {
      name: 'transaction 1',
      description: 'description 1',
      amount: 144,
      date: '15 Dec 2025'
    },
    {
      name: 'transaction 2',
      description: 'description 2',
      amount: 244,
      date: '16 Dec 2025'
    },
    {
      name: 'transaction 3',
      description: 'description 3',
      amount: 344,
      date: '17 Dec 2025'
    },
    {
      name: 'transaction 4',
      description: 'description 4',
      amount: 444,
      date: '18 Dec 2025'
    },
    {
      name: 'transaction 5',
      description: 'description 5',
      amount: 544,
      date: '19 Dec 2025'
    }
  ]

  data2: ITransactionElement[] = [
    {
      name: 'transaction 11',
      description: 'description 11',
      amount: 5000,
      date: '15 Dec 2025'
    },
    {
      name: 'transaction 22',
      description: 'description 22',
      amount: 3600,
      date: '16 Dec 2025'
    }
  ]

  data3: ITransactionElement[] = [
    {
      name: 'transaction 111',
      description: 'description 111',
      amount: 15000,
      date: '15 Dec 2025'
    }
  ]


  activeTab: Tabs = 'all';

  currentData: ITransactionElement[] = this.data;

  setTab(tab: Tabs) {

    this.activeTab = tab;

    switch (tab) {

      case 'all':
        this.currentData = this.data;
        break;

      case 'income':
        this.currentData = this.data2;
        break;

      case 'expense':
        this.currentData = this.data3;
        break;
    }
  }



}
