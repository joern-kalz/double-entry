import * as moment from 'moment';
import { AccountHierarchyNode } from '../account-hierarchy/account-hierarchy-node';

export interface ViewTransactionEntry {
    amount: number;
    account: AccountHierarchyNode;
    verified: boolean;
}

export interface ViewTransaction {
    id: number;
    date: moment.Moment;
    name: string;
    creditEntries: ViewTransactionEntry[];
    debitEntries: ViewTransactionEntry[];
    total: number;
}
