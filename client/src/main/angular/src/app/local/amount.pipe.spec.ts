import { AmountPipe } from './amount.pipe';
import { LocalService } from './local.service';

describe('AmountPipe', () => {
  let pipe: AmountPipe;
  let localService: jasmine.SpyObj<LocalService>;

  beforeEach(() => {
    localService = jasmine.createSpyObj('LocalService', ['formatAmount'])
    pipe = new AmountPipe(localService);
  });

  it('should format amount', () => {
    localService.formatAmount.and.returnValue('12,34');
    expect(pipe.transform(12.34)).toEqual('12,34');
  });
});
