import { Injectable } from '@angular/core';
import { LocalService } from '../local/local.service';
import { ContextTransaction, TransactionType } from './context-transaction';
import { ContextVerification } from './context-verification';
import * as moment from 'moment';

@Injectable({
  providedIn: 'root'
})
export class ContextService {

  transaction: ContextTransaction;
  verification: ContextVerification;

  constructor(
    private localService: LocalService,
  ) { }

  setTransaction(transaction: ContextTransaction) {
    this.transaction = transaction;
  }

  createTransaction(type: TransactionType) {
    this.transaction = {
      type,
      date: this.localService.formatDate(moment()),
      name: '',
      creditEntries: [{
        account: null,
        amount: ''
      }],
      debitEntries: [{
        account: null,
        amount: ''
      }],
    };
  }

  setVerification(verification: ContextVerification) {
    this.verification = verification;
  }
}
