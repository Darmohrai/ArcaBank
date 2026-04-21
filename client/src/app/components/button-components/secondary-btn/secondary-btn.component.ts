import {Component, Input} from '@angular/core';
import {NgIf} from "@angular/common";

@Component({
  selector: 'app-secondary-btn',
  standalone: true,
  imports: [
    NgIf
  ],
  templateUrl: './secondary-btn.component.html',
  styleUrl: './secondary-btn.component.css'
})
export class SecondaryBtnComponent {
  @Input() btnText!: string;
  @Input() icon?: string;
}
