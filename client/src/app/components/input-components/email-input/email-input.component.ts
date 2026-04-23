import {Component, forwardRef, Input, booleanAttribute} from '@angular/core';
import {ControlValueAccessor, NG_VALUE_ACCESSOR, ReactiveFormsModule} from '@angular/forms';

@Component({
  selector: 'app-email-input',
  standalone: true,
  imports: [
    ReactiveFormsModule
  ],
  templateUrl: './email-input.component.html',
  styleUrl: './email-input.component.css',
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => EmailInputComponent),
      multi: true,
    },
  ],
})
export class EmailInputComponent implements ControlValueAccessor {
  @Input() inputId?: string;
  /** Back-compat for templates that used `id` on the host in older screens */
  @Input() id?: string;
  @Input() placeholder: string = '';
  @Input() autocomplete: string = 'email';
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
