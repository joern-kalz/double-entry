import { Injectable, EventEmitter } from '@angular/core';
import { TransactionType } from './transaction-type.enum';

export class TransactionEntry {
  accountId: number;
  amountUser?: string;
  amount: number;
}

export class Transaction {
  transactionType?: TransactionType;
  id?: number;
  dateUser?: string;
  date?: string;
  name?: string;
  entries?: TransactionEntry[];
}

@Injectable({
  providedIn: 'root'
})
export class SessionService {

  transaction: Transaction = null;
  
}
