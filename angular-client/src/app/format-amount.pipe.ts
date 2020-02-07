import { Pipe, PipeTransform } from '@angular/core';
import { LocalService } from './local.service';

@Pipe({
  name: 'formatAmount'
})
export class FormatAmountPipe implements PipeTransform {

  constructor(
    private localService: LocalService
  ) { }

  transform(value: any, args?: any): any {
    return this.localService.formatAmount(value);
  }

}
