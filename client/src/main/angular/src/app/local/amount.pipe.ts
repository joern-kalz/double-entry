import { Pipe, PipeTransform } from '@angular/core';
import { LocalService } from './local.service';

@Pipe({
  name: 'amount'
})
export class AmountPipe implements PipeTransform {

  constructor(
    private localService: LocalService
  ) { }

  transform(value: number, ...args: unknown[]): string {
    return this.localService.formatAmount(value);
  }

}
