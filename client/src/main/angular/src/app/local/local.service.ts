import { Inject, Injectable, LOCALE_ID } from '@angular/core';
import * as moment from 'moment';
import { FormControl } from '@angular/forms';
import * as numeral from 'numeral';

@Injectable({
  providedIn: 'root'
})
export class LocalService {

  private readonly LOCALE = {
    'en-US': {
      date: 'MM-DD-YYYY',
      month: 'MM / YYYY',
      year: 'YYYY',
    },
    'de-DE': {
      date: 'DD.MM.YYYY',
      month: 'MM / YYYY',
      year: 'YYYY',
    },
  };

  locale = this.LOCALE['en-US'];

  constructor(
    @Inject(LOCALE_ID) private localeId: string,
  ) { 
    this.registerLocales();

    if (localeId != 'en-US') {
      numeral.locale(localeId);
      this.locale = this.LOCALE[localeId];
    }
  }

  formatAmount(value: number): string {
    if (value == null) return '';
    return numeral(value).format('0,0.00')
  }

  parseAmount(value: string): number {
    if (this.isEmpty(value)) return null;
    const parsed = numeral(value).value();
    return parsed != NaN ? parsed : null;
  }

  createAmountValidator() {
    return (control: FormControl) => {
      if (this.isEmpty(control.value)) return null;
      return numeral.validate(control.value) ? null : { amount: true };
    };
  }

  formatDate(value: moment.Moment, format?: string): string {
    if (value == null) return '';
    return value.format(this.locale.date);
  }

  parseDate(value: string): moment.Moment {
    if (this.isEmpty(value)) return null;
    const date = moment(value, this.locale.date);
    return date.isValid() ? date : null;
  }

  createDateValidator() {
    return (control: FormControl) => {
      if (this.isEmpty(control.value)) return null;
      const valid = moment(control.value, this.locale.date).isValid();
      return valid ? null : { date: true };
    };
  }

  formatMonth(value: moment.Moment): string {
    if (value == null) return '';
    return value.format(this.locale.month);
  }

  parseMonth(value: string): moment.Moment {
    if (this.isEmpty(value)) return null;
    const splitted = value.split('/').map(v => v.trim());
    if (splitted.length != 2 || !this.isInteger(splitted[0]) || !this.isInteger(splitted[1])) return null;
    const month = +splitted[0];
    const year = +splitted[1];
    if (month < 1 || month > 12) return null;
    return moment([year, month - 1, 1]);
  }

  private isInteger(value: string) {
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
    return value.format(this.locale.year);
  }

  parseYear(value: string): moment.Moment {
    if (this.isEmpty(value)) return null;
    const trimmed = value.trim();
    if (!this.isInteger(trimmed)) return null;
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

  private registerLocales() {
    numeral.register('locale', 'de-DE', {
      delimiters: {
        thousands: '.',
        decimal: ','
      },
      abbreviations: {
        thousand: 'k',
        million: 'm',
        billion: 'b',
        trillion: 't'
      },
      ordinal: function (number) {
        return '.';
      },
      currency: {
        symbol: '€'
      }
    });
  }
}
