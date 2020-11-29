import { Injectable } from '@angular/core';
import * as moment from 'moment';
import { FormControl } from '@angular/forms';

@Injectable({
  providedIn: 'root'
})
export class LocalService {

  private readonly DATE_FORMAT = 'DD.MM.YYYY';
  private readonly MONTH_FORMAT = 'MM / YYYY';
  private readonly YEAR_FORMAT = 'YYYY';
  private readonly AMOUNT_FORMAT = /^([-+]?)(\d+)(\,(\d+))?$/;

  constructor() { }

  formatAmount(value: number): string {
    if (value == null) return '';
    const sign = value < 0 ? '-' : '';
    const absoluteValue = Math.abs(value);
    const integerPart = Math.trunc(absoluteValue)
    const fractionalPart = Math.round(absoluteValue * 100) % 100;
    const paddedFractionalPart = fractionalPart < 10 ? '0' + fractionalPart : fractionalPart;
    return sign + integerPart + ',' + paddedFractionalPart;
  }

  parseAmount(value: string): number {
    if (this.isEmpty(value)) return null;
    const matcher = value.match(this.AMOUNT_FORMAT);
    if (!matcher) return null;
    return +value.replace(',', '.');
  }

  createAmountValidator() {
    return (control: FormControl) => {
      if (this.isEmpty(control.value)) return null;
      const valid = control.value.match(this.AMOUNT_FORMAT) != null;
      return valid ? null : { amount: true };
    };
  }

  formatDate(value: moment.Moment, format?: string): string {
    if (value == null) return '';
    return value.format(this.DATE_FORMAT);
  }

  parseDate(value: string): moment.Moment {
    if (this.isEmpty(value)) return null;
    const date = moment(value, this.DATE_FORMAT);
    return date.isValid() ? date : null;
  }

  createDateValidator() {
    return (control: FormControl) => {
      if (this.isEmpty(control.value)) return null;
      const valid = moment(control.value, this.DATE_FORMAT).isValid();
      return valid ? null : { date: true };
    };
  }

  formatMonth(value: moment.Moment): string {
    if (value == null) return '';
    return value.format(this.MONTH_FORMAT);
  }

  parseMonth(value: string): moment.Moment {
    if (this.isEmpty(value)) return null;
    const matcher = value.match(/^\s*(\d+)\s*\/\s*(\d+)\s*$/);
    if (!matcher) return null;
    const month = +matcher[1];
    const year = +matcher[2];
    if (month < 1 || month > 12) return null;
    return moment([year, month - 1, 1]);
  }

  createMonthValidator() {
    return (control: FormControl) => {
      if (this.isEmpty(control.value)) return null;
      const parsed = this.parseMonth(control.value);
      return parsed != null ? null : { month: true };
    };
  }

  formatYear(value: moment.Moment): string {
    if (value == null) return '';
    return value.format(this.YEAR_FORMAT);
  }

  parseYear(value: string): moment.Moment {
    if (this.isEmpty(value)) return null;
    const matcher = value.match(/^\s*(\d+)\s*$/);
    if (!matcher) return null;
    const year = +matcher[1];
    return moment([year, 0, 1]);
  }

  createYearValidator() {
    return (control: FormControl) => {
      if (this.isEmpty(control.value)) return null;
      const parsed = this.parseYear(control.value);
      return parsed != null ? null : { month: true };
    };
  }

  isEmpty(value: string) {
    return value == null || value.trim() == '';
  }

}
