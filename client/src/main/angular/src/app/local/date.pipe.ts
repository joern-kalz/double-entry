import { Pipe, PipeTransform } from '@angular/core';
import { LocalService } from './local.service';

@Pipe({
  name: 'date'
})
export class DatePipe implements PipeTransform {

  constructor(
    private localService: LocalService
  ) { }

  transform(value: moment.Moment, format?: string): string {
    return format == 'year' ? this.localService.formatYear(value) :
      format == 'month' ? this.localService.formatMonth(value) :
      this.localService.formatDate(value);
  }

}
