import { Component, OnInit, inject } from '@angular/core';
import { BalanceElementComponent } from "../../components/overview-components/balance-element/balance-element.component";
import { GoalsElementComponent } from "../../components/overview-components/goals-element/goals-element.component";
import { RecentTransactionsComponent } from "../../components/overview-components/recent-transactions/recent-transactions.component";
import { TransactionService } from '../../services/TransactionService';
import { BaseChartDirective } from 'ng2-charts';
import { ChartConfiguration, ChartData } from 'chart.js';

@Component({
  selector: 'app-overview',
  standalone: true,
  imports: [
    BalanceElementComponent,
    GoalsElementComponent,
    RecentTransactionsComponent,
    BaseChartDirective
  ],
  templateUrl: './overview.component.html',
  styleUrl: './overview.component.css'
})
export class OverviewComponent implements OnInit {
  private transactionService = inject(TransactionService);

  // Налаштування графіка
  public barChartData: ChartData<'bar'> = {
    labels: [],
    datasets: [
      { data: [], label: 'Доходи', backgroundColor: '#42A5F5' },
      { data: [], label: 'Витрати', backgroundColor: '#FFA726' }
    ]
  };

  public barChartOptions: ChartConfiguration<'bar'>['options'] = {
    responsive: true,
    maintainAspectRatio: false
  };

  ngOnInit() {
    this.transactionService.getMonthlyStats().subscribe({
      next: (stats) => {
        this.barChartData = {
          labels: stats.map(s => `Місяць ${s.month}`),
          datasets: [
            { data: stats.map(s => s.income), label: 'Доходи', backgroundColor: '#42A5F5' },
            { data: stats.map(s => s.expense), label: 'Витрати', backgroundColor: '#FFA726' }
          ]
        };
      },
      error: (err) => console.error("Помилка завантаження статистики", err)
    });
  }
}
