export interface getAllCardsResponse {
  "id": string,
  "accountId": string,
  "cardNumber": string,
  "cardHolderName": string,
  "expirationDate": string,
  "status": string
}


export interface createdCardResponse {
  "id": string,
  "accountId": string,
  "cardNumber": string,
  "cardHolderName": string,
  "expirationDate": string,
  "status": string
}


export interface cardBlockUnblockMessageResponse {
  message: string;
}

export interface Transaction {
  id: string;
  senderAccountId: string;
  receiverAccountId: string;
  amount: number;
  currency: string;
  status: string;
  createdAt: string;
  type: string;
}

export interface getCardTransactionsResponse {
  content: Transaction[];

  pageNumber: number;
  pageSize: number;

  totalElements: number;
  totalPages: number;
}
