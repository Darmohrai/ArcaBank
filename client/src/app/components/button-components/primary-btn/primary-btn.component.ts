import {Component, Input} from '@angular/core';
import {NgIf} from "@angular/common";

@Component({
  selector: 'app-primary-btn',
  standalone: true,
  imports: [
    NgIf
  ],
  templateUrl: './primary-btn.component.html',
  styleUrl: './primary-btn.component.css'
})
export class PrimaryBtnComponent {
  @Input() btnText!: string;
  @Input() icon?: string;
  @Input() btnType: "button" | "submit" | "reset" = "button";
}
