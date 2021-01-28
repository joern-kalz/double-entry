export enum EntryType {
    CREDIT_ACCOUNTS = 'credit-accounts',
    DEBIT_ACCOUNTS = 'debit-accounts',
}

export enum TransactionType {
    GENERIC,
    TRANSFER,
    EXPENSE,
    REVENUE,
}

export interface ContextTransactionEntry {
    amount: string;
    account: number;
}

export interface ContextTransaction {
    id?: number;
    type?: TransactionType;
    date: string;
    name: string;
    creditEntries: ContextTransactionEntry[];
    debitEntries: ContextTransactionEntry[];
}
