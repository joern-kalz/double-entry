import { TestBed } from '@angular/core/testing';

import { AccountListsService } from './account-lists.service';

describe('AccountListsService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: AccountListsService = TestBed.get(AccountListsService);
    expect(service).toBeTruthy();
  });
});
