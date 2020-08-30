import { Injectable } from '@angular/core';
import { ContextTransaction } from './context-transaction';

@Injectable({
  providedIn: 'root'
})
export class ContextService {

  transaction: ContextTransaction;

  constructor() { }

  setTransaction(transaction: ContextTransaction) {
    this.transaction = transaction;
  }
}
