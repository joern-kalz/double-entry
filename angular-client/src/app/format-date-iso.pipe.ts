import { Pipe, PipeTransform } from '@angular/core';
import { LocalService } from './local.service';

@Pipe({
  name: 'formatDateIso'
})
export class FormatDateIsoPipe implements PipeTransform {

  constructor(
    private localService: LocalService
  ) { }

  transform(value: any, args?: any): any {
    return this.localService.formatDate(value);
  }

}
