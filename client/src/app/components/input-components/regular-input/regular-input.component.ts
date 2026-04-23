import {Component, forwardRef, Input, booleanAttribute} from '@angular/core';
import {ControlValueAccessor, NG_VALUE_ACCESSOR} from '@angular/forms';

@Component({
  selector: 'app-regular-input',
  standalone: true,
  imports: [],
  templateUrl: './regular-input.component.html',
  styleUrl: './regular-input.component.css',
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => RegularInputComponent),
      multi: true,
    },
  ],
})
export class RegularInputComponent implements ControlValueAccessor {
  @Input() inputId?: string;
  /** Back-compat for templates that used `id` on the host in older screens */
  @Input() id?: string;
  @Input() placeholder: string = '';
  @Input() type: string = 'text';
  @Input() autocomplete: string = 'off';
  @Input() inputMode?: 'text' | 'search' | 'email' | 'tel' | 'url' | 'none' | 'numeric' | 'decimal';
  @Input({ transform: booleanAttribute }) disabled: boolean = false;

  value: string = '';
  onChange: (value: string) => void = () => {};
  onTouched: () => void = () => {};

  writeValue(value: string | null): void {
    this.value = value ?? '';
  }

  registerOnChange(fn: (value: string) => void): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    this.disabled = isDisabled;
  }

  onInput(value: string): void {
    this.value = value;
    this.onChange(this.value);
  }

  get resolvedId(): string | undefined {
    return this.inputId ?? this.id;
  }
}
