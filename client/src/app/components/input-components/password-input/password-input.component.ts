import {Component, forwardRef, Input, booleanAttribute} from '@angular/core';
import {PasswordModule} from "primeng/password";
import {ControlValueAccessor, FormsModule, NG_VALUE_ACCESSOR, ReactiveFormsModule} from "@angular/forms";

@Component({
  selector: 'app-password-input',
  standalone: true,
  imports: [
    PasswordModule,
    ReactiveFormsModule,
    FormsModule,
  ],
  templateUrl: './password-input.component.html',
  styleUrl: './password-input.component.css',
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => PasswordInputComponent),
      multi: true,
    },
  ],
})
export class PasswordInputComponent implements ControlValueAccessor {
  @Input() inputId?: string;
  /** Back-compat for templates that used `id` on the host in older screens */
  @Input() id?: string;
  @Input() placeholder: string = '';
  @Input() autocomplete: string = 'new-password';
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

  onModelChange(value: string | null): void {
    this.value = value ?? '';
    this.onChange(this.value);
  }

  get resolvedId(): string | undefined {
    return this.inputId ?? this.id;
  }
}
