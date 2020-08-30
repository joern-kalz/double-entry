import * as moment from 'moment';
import { AccountHierarchyNode } from '../account-hierarchy/account-hierarchy-node';
import { TransactionEntries } from '../generated/openapi/model/models';

export interface ContextTransactionEntry {
    amount: string;
    account: AccountHierarchyNode;
}

export interface ContextTransaction {
    id: number;
    date: string;
    name: string;
    creditEntries: ContextTransactionEntry[];
    debitEntries: ContextTransactionEntry[];
}
