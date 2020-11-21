import { TestBed } from '@angular/core/testing';

import { ContextService } from './context.service';

describe('ContextService', () => {
  let service: ContextService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ContextService);
  });

  it('should set transaction', () => {
    const TRANSACTION_MOCK = {};
    service.setTransaction(TRANSACTION_MOCK as any);
    expect(service.transaction).toBe(TRANSACTION_MOCK as any);
  });

  it('should set verification', () => {
    const VERIFICATION_MOCK = {};
    service.setVerification(VERIFICATION_MOCK as any);
    expect(service.verification).toBe(VERIFICATION_MOCK as any);
  });
});
