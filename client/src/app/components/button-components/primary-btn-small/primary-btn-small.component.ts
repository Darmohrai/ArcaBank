import {Component, Input} from '@angular/core';
import {NgIf} from "@angular/common";

@Component({
  selector: 'app-primary-btn-small',
  standalone: true,
  imports: [
    NgIf
  ],
  templateUrl: './primary-btn-small.component.html',
  styleUrl: './primary-btn-small.component.css'
})
export class PrimaryBtnSmallComponent {
  @Input() btnText!: string;
  @Input() icon?: string;
}
