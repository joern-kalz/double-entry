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
    return this.localService.formatDate(value, format);
  }

}
