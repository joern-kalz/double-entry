import { TestBed } from '@angular/core/testing';

import { AccountHierarchyService } from './account-hierarchy.service';

describe('AccountHierarchyService', () => {
  let service: AccountHierarchyService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(AccountHierarchyService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
