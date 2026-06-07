export interface ChestCreationRequest {
  name: string;
  targetAmount: number;
  description?: string;
  friendPhones?: string[];
  pin: string;
  currency?: string;
}

export interface ChestDepositRequest {
  senderAccountId: string;
  amount: number;
}

export interface EscrowInitiationRequest {
  amount: number;
  destinationAccount: string;
  purpose: string;
}

export interface VoteRequest {
  decision: boolean;
}
