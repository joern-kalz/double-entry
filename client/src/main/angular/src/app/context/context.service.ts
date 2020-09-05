import { Injectable } from '@angular/core';
import { ContextTransaction } from './context-transaction';
import { ContextVerification } from './context-verification';

@Injectable({
  providedIn: 'root'
})
export class ContextService {

  transaction: ContextTransaction;
  verification: ContextVerification;

  constructor() { }

  setTransaction(transaction: ContextTransaction) {
    this.transaction = transaction;
  }

  setVerification(verification: ContextVerification) {
    this.verification = verification;
  }
}
