export interface TransferFundsRequest {
  senderSourceId: string,
  sourceType: 'CARD' | 'ACCOUNT',
  destination: string,
  amount: number
}

export interface TransferExchangeRequest {
  "fromAccountId": string,
  "toAccountId": string,
  "amount": number
}
