import {Component, EventEmitter, Input, Output} from '@angular/core';

@Component({
  selector: 'app-sidebar-btn',
  standalone: true,
  imports: [],
  templateUrl: './sidebar-btn.component.html',
  styleUrl: './sidebar-btn.component.css'
})
export class SidebarBtnComponent {
  @Input() text!: string;
  @Input() icon!: string;

  @Output() btnClicked = new EventEmitter<void>();

  onClick() {
    this.btnClicked.emit();
  }

}
