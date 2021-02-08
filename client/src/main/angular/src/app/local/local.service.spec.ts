import { TestBed } from '@angular/core/testing';

import { LocalService } from './local.service';
import * as moment from 'moment';

describe('LocalService', () => {
  let service: LocalService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(LocalService);
  });

  it('should format amount', () => {
    expect(service.formatAmount(1.23)).toEqual('1.23');
  });

  it('should format date', () => {
    expect(service.formatDate(moment('2020-01-01'))).toEqual('01-01-2020');
  });

  it('should parse amount', () => {
    expect(service.parseAmount('1.23')).toEqual(1.23);
  });

  it('should parse negative amount', () => {
    expect(service.parseAmount('-1.23')).toEqual(-1.23);
  });

  it('should validate amount', () => {
    const formControlMock = jasmine.createSpyObj('FormControl', [], {value: '1.23'})
    expect(service.createAmountValidator()(formControlMock)).toBeNull();
  });

  it('should validate negative amount', () => {
    const formControlMock = jasmine.createSpyObj('FormControl', [], {value: '-1.23'})
    expect(service.createAmountValidator()(formControlMock)).toBeNull();
  });

  it('should parse date', () => {
    expect(service.parseDate('01-01-2020').format('YYYY-MM-MM')).toEqual('2020-01-01');
  });
});
