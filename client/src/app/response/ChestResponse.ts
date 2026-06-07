export interface ChestCreationResponse {
  id: string;
  name: string;
  targetAmount: number;
  status: string;
  iban?: string;
  cardNumber?: string;
}

export interface ChestListResponse {
  id: string;
  accountId?: string;
  name: string;
  targetAmount: number;
  description?: string;
  currency?: string;
  balance?: number;
  frozenBalance?: number;
  status: string;
  createdAt?: string;
}

export interface ChestDepositResponse {
  message: string;
  chestId: string;
  newBalance: number;
}

export interface MessageResponse {
  message: string;
}

export interface ChestMemberViewModel {
  id?: string;
  userId?: string;
  phone?: string;
  fullName?: string;
  role?: 'OWNER' | 'TRUSTEE' | string;
  joinedAt?: string;
}

export interface PendingEscrowResponse {
  id: string;
  chestId: string;
  initiatorId: string;
  amount: number;
  destinationAccount: string;
  purpose: string;
  status: 'PENDING' | 'APPROVED' | 'REJECTED' | string;
  createdAt?: string;
  approvalsCount: number;
  trusteesCount: number;
  currentUserVoted: boolean;
  canCurrentUserVote: boolean;
}

export interface ChestDetailResponse {
  id: string;
  name: string;
  targetAmount: number;
  balance: number;
  currency: string;
  status: string;
  members: ChestMemberViewModel[];
  escrows: PendingEscrowResponse[];
}

export interface EscrowInitiationResponse {
  message: string;
  escrowId: string;
  chestId: string;
  amount: number;
  status: string;
}

export interface ChestViewModel extends ChestCreationResponse {
  accountId?: string;
  description?: string;
  currency?: string;
  currentBalance: number;
  frozenBalance: number;
  pendingVote?: boolean;
  escrowId?: string;
  pendingEscrow?: PendingEscrowResponse;
  escrows?: PendingEscrowResponse[];
  members?: ChestMemberViewModel[];
  memberPhones?: string[];
}
