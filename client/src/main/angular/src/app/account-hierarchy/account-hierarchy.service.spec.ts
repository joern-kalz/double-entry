import { TestBed } from '@angular/core/testing';
import { Account } from '../generated/openapi/model/models';
import { AccountType } from './account-hierarchy';

import { AccountHierarchyService } from './account-hierarchy.service';

describe('AccountHierarchyService', () => {
  const ACCOUNTS_MOCK: Account[] = [
    { id: 1, name: 'ASSET', active: true, parentId: null },
    { id: 2, name: 'EQUITY', active: true, parentId: null },
    { id: 3, name: 'EXPENSE', active: true, parentId: 2 },
    { id: 4, name: 'REVENUE', active: true, parentId: 2 },
    { id: 5, name: 'cash', active: true, parentId: 1 },
    { id: 6, name: 'bank-account', active: true, parentId: 1 },
  ];

  let service: AccountHierarchyService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(AccountHierarchyService);
  });

  it('should create hierarchy accounts', () => {
    const hierarchy = service.createAccountHierarchy(ACCOUNTS_MOCK);
    expect(hierarchy.accountsById.size).toEqual(6);
    expect(hierarchy.accountsById.get(1).name).toEqual('ASSET');
  });

  it('should create hierarchy roots', () => {
    const hierarchy = service.createAccountHierarchy(ACCOUNTS_MOCK);
    expect(hierarchy.root.size).toEqual(4);
    expect(hierarchy.root.get(AccountType.EQUITY).name).toEqual('EQUITY');
  });

  it('should create hierarchy lists', () => {
    const hierarchy = service.createAccountHierarchy(ACCOUNTS_MOCK);
    expect(hierarchy.list.size).toEqual(5);
    expect(hierarchy.list.get(AccountType.ALL)[0].name).toEqual('ASSET');
  });

  it('should create roots', () => {
    const rootAccounts = service.createRootAccounts();
    expect(rootAccounts.length).toEqual(4);
    expect(rootAccounts[0].name).toEqual('ASSET');
  });
});
