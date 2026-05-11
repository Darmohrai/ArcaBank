export interface CreateAccountRequest {
  currency: 'UAH' | 'USD' | 'EUR';
  type: 'DEBIT' | 'CREDIT' | 'VIRTUAL';
}
