import { Injectable } from '@angular/core';
import { LocalService } from './local.service';
import { Validators, FormControl, FormGroup } from '@angular/forms';

@Injectable({
  providedIn: 'root'
})
export class FormValidatorService {

  constructor(
    private localService: LocalService
  ) { }

  date() {
    return (control: FormControl) => {
      if (this.isEmpty(control.value)) return null;
      const valid = this.localService.parseDate(control.value) != null;
      return valid ? null : { date: true };
    };
  }

  amount() {
    return (control: FormControl) => {
      if (this.isEmpty(control.value)) return null;
      const valid = this.localService.parseAmount(control.value) != null;
      return valid ? null : { amount: true };
    };
  }

  isEmpty(value: string) {
    return value == null || value.trim() == '';
  }

}
