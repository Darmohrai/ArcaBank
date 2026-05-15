export interface ExchangeRateResponse {
  id: string;
  currency: string;
  baseCurrency: string;
  buyRate: number;
  sellRate: number;
  updatedAt: string;
}
