import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class LocalService {

  constructor() { }

  parseDate(text: string) : string {
    if (!text) return null;
    
    const match = text.match(/^\s*(\d{1,2}).(\d{1,2}).(\d{2,4})\s*$/);
    if (!match) return null;

    const day = +match[1];
    const month = +match[2];
    let year = +match[3];

    if (year < 100) year += Math.trunc(new Date().getFullYear() / 100) * 100;

    if(month < 1 || month > 12) return null;

    const DAYS_IN_MONTH = [ 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 ];
    if (year % 400 == 0 || (year % 100 != 0 && year % 4 == 0)) DAYS_IN_MONTH[1] = 29;
    if (day < 1 || day > DAYS_IN_MONTH[month - 1]) return null;

    return `${this.prependZero(year, 4)}-${this.prependZero(month, 2)}-${this.prependZero(day, 2)}`;
  }

  fromDate(date: Date) {
    const day = date.getDate();
    const month = date.getMonth() + 1;
    let year = date.getFullYear();

    return `${this.prependZero(year, 4)}-${this.prependZero(month, 2)}-${this.prependZero(day, 2)}`;
  }

  formatDate(dateIso: string): string {
    const match = dateIso.match(/^(\d\d\d\d)-(\d\d)-(\d\d)$/);
    return match ? `${match[3]}.${match[2]}.${match[1]}` : null;
  }

  parseAmount(text: string) {
    if (!text) return null;
    const normalized = text.replace(',', '.').trim();
    if (normalized == '') return null;
    const value = +normalized;
    return isNaN(value) ? null : value;
  }

  formatAmount(amount: number) {
    const options = { minimumFractionDigits: 2, maximumFractionDigits: 2, useGrouping: false };
    const amountFormat = new Intl.NumberFormat('de-DE', options);
    return amountFormat.format(amount);
  }

  private prependZero(value: number, digits: number): string {
    let result = '' + value;
    while (result.length < digits) result = '0' + result;
    return result;
  }

}
