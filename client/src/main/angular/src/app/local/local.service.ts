import { Injectable } from '@angular/core';
import * as moment from 'moment';
import { FormControl } from '@angular/forms';

@Injectable({
  providedIn: 'root'
})
export class LocalService {

  private readonly DATE_FORMAT = 'DD.MM.YYYY';
  private readonly AMOUNT_FORMAT = /^([-+]?)(\d+)(\,(\d+))?$/;

  constructor() { }

  formatAmount(value: number): string {
    const sign = value < 0 ? '-' : '';
    const absoluteValue = Math.abs(value);
    const integerPart = Math.trunc(absoluteValue)
    const fractionalPart = Math.round(absoluteValue * 100) % 100;
    const paddedFractionalPart = fractionalPart < 10 ? '0' + fractionalPart : fractionalPart;
    return sign + integerPart + ',' + paddedFractionalPart;
  }

  formatDate(value: moment.Moment): string {
    return value.format(this.DATE_FORMAT);
  }

  parseAmount(value: string): number {
    if (this.isEmpty(value)) return null;
    const matcher = value.match(this.AMOUNT_FORMAT);
    if (!matcher) return null;
    const sign = matcher[1] == '-' ? -1 : 1;
    const integerPart = +matcher[2];
    const fractionalPart = matcher[3] ? +matcher[4] / Math.pow(10, matcher[4].length) : 0;
    return sign * (integerPart + fractionalPart);
  }

  parseDate(value: string): moment.Moment {
    if (this.isEmpty(value)) return null;
    return moment(value, this.DATE_FORMAT);
  }

  createDateValidator() {
    return (control: FormControl) => {
      if (this.isEmpty(control.value)) return null;
      const valid = moment(control.value, this.DATE_FORMAT).isValid();
      return valid ? null : { date: true };
    };
  }

  createAmountValidator() {
    return (control: FormControl) => {
      if (this.isEmpty(control.value)) return null;
      const valid = control.value.match(this.AMOUNT_FORMAT) != null;
      return valid ? null : { amount: true };
    };
  }

  isEmpty(value: string) {
    return value == null || value.trim() == '';
  }

}
