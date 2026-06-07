import { Component, EventEmitter, inject, Input, OnChanges, Output } from '@angular/core';
import { NgIf } from '@angular/common';
import { catchError, finalize, throwError } from 'rxjs';

import { ChestService } from '../../../services/ChestService';

@Component({
  selector: 'app-chest-voting-widget',
  standalone: true,
  imports: [NgIf],
  templateUrl: './chest-voting-widget.component.html',
  styleUrl: './chest-voting-widget.component.css',
})
export class ChestVotingWidgetComponent implements OnChanges {
  @Input({ required: true }) escrowId!: string;
  @Input() voteProgress = 'Очікується голосування учасників';
  @Input() alreadyVoted = false;
  @Output() voted = new EventEmitter<void>();

  private chestService = inject(ChestService);

  isSubmitting = false;
  serverError: string | null = null;
  hasVoted = false;

  ngOnChanges(): void {
    this.hasVoted = this.alreadyVoted;
  }

  approve(): void {
    this.submitVote(true);
  }

  reject(): void {
    this.submitVote(false);
  }

  private submitVote(decision: boolean): void {
    if (this.isSubmitting || this.hasVoted) {
      return;
    }

    this.isSubmitting = true;
    this.serverError = null;

    this.chestService
      .voteEscrow(this.escrowId, { decision })
      .pipe(
        catchError((error) => {
          const title = error.error?.title as string | undefined;
          const detail = error.error?.detail as string | undefined;

          if (title === 'ALREADY_VOTE') {
            this.serverError = 'Ви вже проголосували за цей запит.';
          } else if (title === 'NOT_CHEST_ACCESS') {
            this.serverError = 'Голосування доступне лише довіреним учасникам.';
          } else {
            this.serverError = detail ?? 'Не вдалося надіслати голос.';
          }
          return throwError(() => error);
        }),
        finalize(() => (this.isSubmitting = false))
      )
      .subscribe({
        next: () => {
          this.hasVoted = true;
          this.voted.emit();
        },
      });
  }
}
