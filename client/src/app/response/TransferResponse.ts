export interface TransferResponse {
  message: string;
  transactionId: string;
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

export interface TransferHistoryResponse {
  content: Transaction[];

  pageNumber: number;
  pageSize: number;

  totalElements: number;
  totalPages: number;
}
