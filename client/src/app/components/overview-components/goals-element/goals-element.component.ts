import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import {GoalResponse, GoalsService} from "../../../services/GoalsService";
import {DialogModule} from "primeng/dialog";
import {InputNumberModule} from "primeng/inputnumber";
import {ButtonModule} from "primeng/button";
import {FormsModule} from "@angular/forms";


@Component({
  selector: 'app-goals-element',
  standalone: true,
  imports: [
    CommonModule,
    DialogModule,
    InputNumberModule,
    ButtonModule,
    FormsModule
  ],
  templateUrl: './goals-element.component.html',
  styleUrl: './goals-element.component.css'
})
export class GoalsElementComponent implements OnInit {
  private goalsService = inject(GoalsService);

  goal: GoalResponse | null = null;

  year = new Date().getFullYear();
  month = new Date().getMonth() + 1;

  isEditGoalDialogVisible = false;
  goalAmountInput = 0;

  monthName = '';

  max = 0;
  value = 0;

  radius = 80;
  circumference = Math.PI * this.radius;

  progress = 0;
  dashOffset = this.circumference;
  angle = -131;

  valueLabel = '₴0';

  ngOnInit(): void {
    this.setMonthName();
    this.loadGoal();
  }

  loadGoal(): void {
    this.goalsService.getGoal(this.year, this.month).subscribe({
      next: (goal) => {
        this.goal = goal;

        this.max = goal.targetAmount;
        this.value = goal.netIncome;

        this.recalculateGauge();
      },
      error: (err) => {
        console.error('Failed to load goal:', err);
      }
    });
  }

  updateGoal(): void {
    const result = prompt('Введіть нову фінансову ціль:', String(this.max));

    if (!result) return;

    const targetAmount = Number(result);

    if (Number.isNaN(targetAmount) || targetAmount < 0) {
      alert('Некоректна сума');
      return;
    }

    this.goalsService.saveGoal({
      year: this.year,
      month: this.month,
      targetAmount
    }).subscribe({
      next: (goal) => {
        this.goal = goal;

        this.max = goal.targetAmount;
        this.value = goal.netIncome;

        this.recalculateGauge();
      },
      error: (err) => {
        console.error('Failed to save goal:', err);
      }
    });
  }

  saveGoal(): void {

    if (this.goalAmountInput < 0) {
      return;
    }

    this.goalsService.saveGoal({
      year: this.year,
      month: this.month,
      targetAmount: this.goalAmountInput
    }).subscribe({
      next: (goal) => {

        this.goal = goal;

        this.max = goal.targetAmount;
        this.value = goal.netIncome;

        this.recalculateGauge();

        this.isEditGoalDialogVisible = false;
      }
    });
  }

  private recalculateGauge(): void {
    if (this.max <= 0) {
      this.progress = 0;
    } else {
      this.progress = Math.min(this.value / this.max, 1);
    }

    this.dashOffset = this.circumference * (1 - this.progress);
    this.angle = -131 + 180 * this.progress;
    this.valueLabel = this.formatMoneyShort(this.value);
  }

  private formatMoneyShort(value: number): string {
    if (value >= 1000) {
      return `$${(value / 1000).toFixed(1)}K`;
    }

    return `$${value.toFixed(0)}`;
  }

  private setMonthName(): void {
    const months = [
      'Січень',
      'Лютий',
      'Березень',
      'Квітень',
      'Травень',
      'Червень',
      'Липень',
      'Серпень',
      'Вересень',
      'Жовтень',
      'Листопад',
      'Грудень'
    ];

    this.monthName = months[this.month - 1];
  }

  openEditGoalDialog(): void {
    this.goalAmountInput = this.max;
    this.isEditGoalDialogVisible = true;
  }
}
