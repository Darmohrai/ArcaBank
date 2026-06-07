import { Component, inject, OnInit } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { CurrencyPipe, NgForOf, NgIf } from '@angular/common';

import { ChestViewModel } from '../../response/ChestResponse';
import { ChestService } from '../../services/ChestService';

@Component({
  selector: 'app-chest-list',
  standalone: true,
  imports: [NgForOf, NgIf, CurrencyPipe, RouterLink],
  templateUrl: './chest-list.component.html',
  styleUrl: './chest-list.component.css',
})
export class ChestListComponent implements OnInit {
  private router = inject(Router);
  private chestService = inject(ChestService);

  chests: ChestViewModel[] = [];
  isLoading = true;
  serverError: string | null = null;

  ngOnInit(): void {
    this.loadChests();
  }

  loadChests(): void {
    this.isLoading = true;
    this.serverError = null;

    this.chestService.getMyChests().subscribe({
      next: (chests) => {
        this.chests = chests.map((chest) => ({
          ...chest,
          currentBalance: chest.balance ?? 0,
          frozenBalance: chest.frozenBalance ?? 0,
          currency: chest.currency ?? 'UAH',
        }));
        this.isLoading = false;
      },
      error: () => {
        this.serverError = 'Не вдалося завантажити скрині.';
        this.isLoading = false;
      },
    });
  }

  openChest(id: string): void {
    this.router.navigate(['/chests', id]);
  }

  getProgress(chest: ChestViewModel): number {
    if (chest.targetAmount <= 0) return 0;
    return Math.min(100, (chest.currentBalance / chest.targetAmount) * 100);
  }
}
