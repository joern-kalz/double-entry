import { TestBed } from '@angular/core/testing';
import { AccountHierarchy } from '../account-hierarchy/account-hierarchy';
import { Transaction } from '../generated/openapi/model/models';
import * as moment from 'moment';

import { ViewTransactionFactoryService } from './view-transaction-factory.service';

describe('ViewTransactionFactoryService', () => {
  let service: ViewTransactionFactoryService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ViewTransactionFactoryService);
  });

  it('should create ViewTransaction', () => {
    const transaction: Transaction = {
      id: 1, date: '2020-01-01', name: 'transaction-name', entries: [
        { accountId: 1, amount: '10', verified: false },
        { accountId: 2, amount: '-10', verified: false },
      ]
    };

    const accountHierarchy: AccountHierarchy = {
      accountsById: new Map([
        [1, {id: 1, name: 'cash', parent: null, children: [], hierarchyLevel: 0, active: true}],
        [2, {id: 2, name: 'expense', parent: null, children: [], hierarchyLevel: 0, active: true}],
      ]),
      root: null,
      list: null
    };

    const result = service.create(transaction, accountHierarchy);

    expect(result).toEqual({
      date: moment('2020-01-01'),
      name: 'transaction-name',
      id: 1,
      creditEntries: [{ account: accountHierarchy.accountsById.get(2), verified: false, amount: 10 }],
      debitEntries: [{ account: accountHierarchy.accountsById.get(1), verified: false, amount: 10 }],
      total: 10
    });
  });
});
