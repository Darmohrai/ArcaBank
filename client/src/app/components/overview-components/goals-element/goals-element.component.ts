import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-goals-element',
  standalone: true,
  imports: [],
  templateUrl: './goals-element.component.html',
  styleUrl: './goals-element.component.css'
})
export class GoalsElementComponent {

  max = 20000;
  value = 13564;

  radius = 80;
  circumference = Math.PI * this.radius;

  progress = this.value / this.max;

  dashOffset =
    this.circumference * (1 - this.progress);

  angle =
    -131 + (180 * this.progress);

  valueLabel = `${(this.value / 1000).toFixed(1)}K`;
}
