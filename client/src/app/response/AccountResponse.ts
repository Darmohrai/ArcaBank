export interface CreateAccountResponse {
 id: string;
 userId: string;
 iban?: string;
 currency: string;
 balance: number;
 status: string;
 createdAt: string;
}



export interface CreateAccountWithCardResponse {
  accountId: string;
  cardId: string;
  iban: string;
  cardNumber: string;
  cardHolderName: string;
  expirationDate: string;
  cvv: string;
  currency: string;
  balance: number;
}


export interface accountBlockUnblockMessageResponse {
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

export interface getAccountTransactionsResponse {
  content: Transaction[];

  pageNumber: number;
  pageSize: number;

  totalElements: number;
  totalPages: number;
}
