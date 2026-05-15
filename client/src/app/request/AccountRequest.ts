export interface CreateAccountRequest {
  currency: 'UAH' | 'USD' | 'EUR';
  type: 'DEBIT' | 'CREDIT' | 'VIRTUAL'| 'CHECKING' | 'SAVINGS';
}

export interface CreateAccountWithCardRequest {
  currency: 'UAH' | 'USD' | 'EUR';
  type: 'DEBIT' | 'CREDIT' | 'VIRTUAL'| 'CHECKING' | 'SAVINGS';
  pin: string;
}
