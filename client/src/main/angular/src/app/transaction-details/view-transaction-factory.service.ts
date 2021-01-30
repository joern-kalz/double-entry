import { Injectable } from '@angular/core';
import { Transaction, TransactionEntries } from '../generated/openapi/model/models';
import { ViewTransaction, ViewTransactionEntry } from './view-transaction';
import * as moment from 'moment';
import { AccountHierarchy } from '../account-hierarchy/account-hierarchy';
import { Converter } from '../api-access/converter';

@Injectable({
  providedIn: 'root'
})
export class ViewTransactionFactoryService {

  constructor() { }

  create(transaction: Transaction, accountHierarchy: AccountHierarchy): ViewTransaction {
    const creditEntries = transaction.entries
      .filter(entry => Converter.parseApiAmount(entry.amount) < 0)
      .map(entry => this.createViewEntry(entry, true, accountHierarchy));

    const debitEntries = transaction.entries
      .filter(entry => Converter.parseApiAmount(entry.amount) >= 0)
      .map(entry => this.createViewEntry(entry, false, accountHierarchy));

    const totalCents = transaction.entries
      .filter(entry => Converter.parseApiAmount(entry.amount) >= 0)
      .reduce((sum, entry) => sum + Math.round(Converter.parseApiAmount(entry.amount) * 100), 0);

    return {
      id: transaction.id,
      date: moment(transaction.date),
      name: transaction.name,
      creditEntries,
      debitEntries,
      total: totalCents / 100
    }
  }

  private createViewEntry(entry: TransactionEntries, isCredit: boolean, 
    accountHierarchy: AccountHierarchy): ViewTransactionEntry {

    return {
      amount: (isCredit ? -1 : 1) * Converter.parseApiAmount(entry.amount),
      account: accountHierarchy.accountsById.get(entry.accountId),
      verified: entry.verified
    }
  }

}
