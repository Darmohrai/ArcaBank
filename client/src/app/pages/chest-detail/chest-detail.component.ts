import { Component, inject, OnInit } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { CurrencyPipe, DecimalPipe, NgForOf, NgIf } from '@angular/common';
import { ToastModule } from 'primeng/toast';
import { MessageService } from 'primeng/api';

import { ChestViewModel, PendingEscrowResponse } from '../../response/ChestResponse';
import { ChestService } from '../../services/ChestService';
import { ChestDepositModalComponent } from '../../components/chest-components/chest-deposit-modal/chest-deposit-modal.component';
import { ChestWithdrawalModalComponent } from '../../components/chest-components/chest-withdrawal-modal/chest-withdrawal-modal.component';
import { ChestVotingWidgetComponent } from '../../components/chest-components/chest-voting-widget/chest-voting-widget.component';

@Component({
  selector: 'app-chest-detail',
  standalone: true,
  imports: [
    NgIf,
    NgForOf,
    CurrencyPipe,
    DecimalPipe,
    ToastModule,
    RouterLink,
    ChestDepositModalComponent,
    ChestWithdrawalModalComponent,
    ChestVotingWidgetComponent,
  ],
  providers: [MessageService],
  templateUrl: './chest-detail.component.html',
  styleUrl: './chest-detail.component.css',
})
export class ChestDetailComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private chestService = inject(ChestService);
  private messageService = inject(MessageService);

  chest: ChestViewModel | null = null;
  isLoading = true;
  notFound = false;
  serverError: string | null = null;

  showDepositModal = false;
  showWithdrawModal = false;

  ngOnInit(): void {
    const chestId = this.route.snapshot.paramMap.get('id');
    if (!chestId) {
      this.notFound = true;
      this.isLoading = false;
      return;
    }

    this.loadChest(chestId);
  }

  loadChest(chestId: string): void {
    this.isLoading = true;
    this.notFound = false;
    this.serverError = null;

    this.chestService.getChestDetails(chestId).subscribe({
      next: (detail) => {
        const pendingEscrow =
          detail.escrows?.find((escrow) => escrow.status === 'PENDING') ?? null;

        this.chest = {
          id: detail.id,
          name: detail.name,
          targetAmount: detail.targetAmount,
          status: detail.status,
          currency: detail.currency ?? 'UAH',
          currentBalance: detail.balance ?? 0,
          frozenBalance: pendingEscrow?.amount ?? 0,
          members: detail.members ?? [],
          escrows: detail.escrows ?? [],
          pendingEscrow: pendingEscrow ?? undefined,
          pendingVote: Boolean(pendingEscrow),
          escrowId: pendingEscrow?.id,
        };
        this.isLoading = false;
      },
      error: (error) => {
        this.notFound = error.status === 404;
        this.serverError = this.notFound
          ? null
          : 'Не вдалося завантажити скриню.';
        this.isLoading = false;
      },
    });
  }

  get progressPercent(): number {
    if (!this.chest || this.chest.targetAmount <= 0) {
      return 0;
    }
    return Math.min(100, (this.chest.currentBalance / this.chest.targetAmount) * 100);
  }

  get activeEscrow(): PendingEscrowResponse | undefined {
    return this.chest?.pendingEscrow;
  }

  get canVote(): boolean {
    return Boolean(this.activeEscrow?.canCurrentUserVote);
  }

  get voteProgress(): string {
    if (!this.activeEscrow) {
      return '';
    }

    return `Схвалено ${this.activeEscrow.approvalsCount} з ${this.activeEscrow.trusteesCount}`;
  }

  get hasMembers(): boolean {
    return Boolean(this.chest?.members?.length);
  }

  get memberRows(): { label: string; role?: string; joinedAt?: string }[] {
    return (this.chest?.members ?? []).map((member) => ({
      label: member.fullName || member.phone || member.userId || member.id || 'Учасник',
      role: member.role,
      joinedAt: member.joinedAt,
    }));
  }

  onDepositSuccess(): void {
    if (!this.chest) {
      return;
    }
    this.loadChest(this.chest.id);
    this.messageService.add({
      severity: 'success',
      summary: 'Успіх!',
      detail: 'Скриню успішно поповнено.',
    });
  }

  onWithdrawInitiated(): void {
    if (!this.chest) {
      return;
    }
    this.loadChest(this.chest.id);
    this.messageService.add({
      severity: 'success',
      summary: 'Запит відправлено!',
      detail: 'Очікуємо голосів довірених учасників.',
    });
  }

  onVoted(): void {
    if (this.chest) {
      this.loadChest(this.chest.id);
    }
    this.messageService.add({
      severity: 'success',
      summary: 'Голос враховано',
      detail: 'Ваш голос успішно враховано.',
    });
  }
}
