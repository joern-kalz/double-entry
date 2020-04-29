import { TestBed } from '@angular/core/testing';

import { TransactionEditorService } from './transaction-editor.service';

describe('TransactionEditorService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: TransactionEditorService = TestBed.get(TransactionEditorService);
    expect(service).toBeTruthy();
  });
});
