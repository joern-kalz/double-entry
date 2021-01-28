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
    if (!this._isAmount(value)) return null;
    return +value.replace(',', '.');
  }

  createAmountValidator() {
    return (control: FormControl) => {
      if (this.isEmpty(control.value)) return null;
      return this._isAmount(control.value) ? null : { amount: true };
    };
  }

  _isAmount(value) {
    let digit = false;
    let i = 0;
    if (i < value.length && ['-', '+'].includes(value[i])) i++;
    while (i < value.length && value[i] >= '0' && value[i] <= '9') { i++; digit = true; }
    if (i < value.length && [','].includes(value[i])) i++;
    while (i < value.length && value[i] >= '0' && value[i] <= '9') { i++; }
    return i == value.length && digit;
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
    const splitted = value.split('/').map(v => v.trim());
    if (splitted.length != 2 || !this._isInteger(splitted[0]) || !this._isInteger(splitted[1])) return null;
    const month = +splitted[0];
    const year = +splitted[1];
    if (month < 1 || month > 12) return null;
    return moment([year, month - 1, 1]);
  }

  _isInteger(value: string) {
    for (let i = 0; i < value.length; i++) {
      if (value[i] < '0' || value[i] > '9') return false;
    }
    return value.length > 0;
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
    const trimmed = value.trim();
    if (!this._isInteger(trimmed)) return null;
    const year = +trimmed;
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
