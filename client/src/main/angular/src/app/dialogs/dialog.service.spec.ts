import { TestBed } from '@angular/core/testing';
import { DialogMessage } from './dialog-message.enum';

import { DialogService } from './dialog.service';

describe('DialogService', () => {
  let service: DialogService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(DialogService);
  });

  it('should show', () => {
    service.show(DialogMessage.CONNECTION_ERROR);
    expect(service.message).toEqual(DialogMessage.CONNECTION_ERROR);
  });

  it('should hide', () => {
    service.show(DialogMessage.CONNECTION_ERROR);
    service.hide();
    expect(service.message).toBeNull();
  });
});
