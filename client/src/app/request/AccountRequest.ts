export interface CreateAccountRequest {
  currency: 'UAH' | 'USD' | 'EUR';
  type: 'CREDIT' | 'CHECKING' | 'SAVINGS';
}

export interface CreateAccountWithCardRequest {
  currency: 'UAH' | 'USD' | 'EUR';
  type: 'CREDIT' | 'CHECKING' | 'SAVINGS';
  pin: string;
}
