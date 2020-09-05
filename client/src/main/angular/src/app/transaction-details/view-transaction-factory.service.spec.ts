import { TestBed } from '@angular/core/testing';

import { ViewTransactionFactoryService } from './view-transaction-factory.service';

describe('ViewTransactionFactoryService', () => {
  let service: ViewTransactionFactoryService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ViewTransactionFactoryService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
