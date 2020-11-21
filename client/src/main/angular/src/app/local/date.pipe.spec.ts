import { DatePipe } from './date.pipe';
import { LocalService } from './local.service';
import * as moment from 'moment';

describe('DatePipe', () => {
  let pipe: DatePipe;
  let localService: jasmine.SpyObj<LocalService>;

  beforeEach(() => {
    localService = jasmine.createSpyObj('LocalService', ['formatDate'])
    pipe = new DatePipe(localService);
  });

  it('should format date', () => {
    localService.formatDate.and.returnValue('01.01.2020');
    expect(pipe.transform(moment('2020-01-01'))).toEqual('01.01.2020');
  });
});
