import { Transaction } from '../response/TransferResponse';

export type TransactionDirection = 'income' | 'expense' | 'neutral';

export function classifyTransaction(
  t: Transaction,
  myAccountIds: ReadonlySet<string>
): TransactionDirection {
  const type = (t.type ?? '').toUpperCase();
  if (type.includes('DEPOSIT')) return 'income';
  if (type.includes('WITHDRAW')) return 'expense';

  const senderMine = myAccountIds.has(t.senderAccountId);
  const receiverMine = myAccountIds.has(t.receiverAccountId);

  if (receiverMine && !senderMine) return 'income';
  if (senderMine && !receiverMine) return 'expense';
  return 'neutral';
}

export function humanizeTransactionType(type: string): string {
  const u = (type ?? '').toUpperCase();
  if (u.includes('DEPOSIT')) return 'Поповнення';
  if (u.includes('EXCHANGE')) return 'Обмін валют';
  if (u.includes('TRANSFER') || u.includes('PAYMENT')) return 'Переказ';
  if (u.includes('WITHDRAW')) return 'Зняття';
  return type || 'Операція';
}

export function shortId(id: string): string {
  if (!id) return '—';
  return id.length <= 10 ? id : `${id.slice(0, 8)}…`;
}

export function transactionSubtitle(
  t: Transaction,
  myAccountIds: ReadonlySet<string>
): string {
  const dir = classifyTransaction(t, myAccountIds);
  if (dir === 'income') {
    return `Від ${shortId(t.senderAccountId)}`;
  }
  if (dir === 'expense') {
    return `На ${shortId(t.receiverAccountId)}`;
  }
  if (myAccountIds.has(t.senderAccountId) && myAccountIds.has(t.receiverAccountId)) {
    return `Між рахунками · ${shortId(t.receiverAccountId)}`;
  }
  return `${shortId(t.senderAccountId)} → ${shortId(t.receiverAccountId)}`;
}

export function amountPrefix(direction: TransactionDirection): '' | '+' | '−' {
  if (direction === 'income') return '+';
  if (direction === 'expense') return '−';
  return '';
}
