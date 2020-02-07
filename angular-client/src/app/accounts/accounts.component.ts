import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { AccountType } from '../account-type';
import { FormBuilder } from '@angular/forms';
import { FormValidatorService } from '../form-validator.service';
import { LocalService } from '../local.service';
import { AccountListsService } from '../account-lists.service';
import { SessionService } from '../session.service';
import { Router } from '@angular/router';
import { BalancesService } from '../server';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-accounts',
  templateUrl: './accounts.component.html',
  styleUrls: ['./accounts.component.scss']
})
export class AccountsComponent implements OnInit {

  accounts: any[];

  showErrors = false;

  form = this.fb.group({
    date: [this.defaultDate, this.fv.date()],
  });

  @ViewChild('date') dateElement: ElementRef;

  constructor(
    private fb: FormBuilder,
    private fv: FormValidatorService,
    private local: LocalService,
    private accountListsService: AccountListsService,
    private sessionService: SessionService,
    private router: Router,
    private balancesService: BalancesService,
  ) { }

  ngOnInit() {
    this.submit();
  }

  get defaultDate(): string {
    return this.fromDate(new Date());
  }

  fromDate(date: Date): string {
    const dateIso = this.local.fromDate(date);
    return this.local.formatDate(dateIso);
  }

  submit() {
    if (this.date.invalid) {
      this.showErrors = true;
      return this.dateElement.nativeElement.focus();
    }

    this.showErrors = false;

    const before = this.local.parseDate(this.date.value);

    forkJoin(
      this.balancesService.balancesFindAll(null, before),
      this.accountListsService.getAccountListsCache()
    )
    .subscribe(([balances, accountListsCache]) => {
      this.accounts = accountListsCache.accountLists.get(AccountType.ASSET).entries.map(account => {
        const balance = balances.find(balance => balance.accountId == account.id);

        return {
          name: account.name,
          amount: balance ? balance.balance : 0,
          level: account.hierarchyLevel
        }
      })
    });
  }

  get date() { return this.form.get('date'); }
}
