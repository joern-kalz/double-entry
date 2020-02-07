import { Injectable, EventEmitter } from '@angular/core';
import { AccountType } from './account-type';
import { BehaviorSubject } from 'rxjs';
import { TransactionType } from './transaction-type.enum';

export class TransactionEntry {
  accountId: number;
  amountUser: string;
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

  transactionChangeEvent = new EventEmitter<void>();

  transaction: Transaction = null;
  
}
