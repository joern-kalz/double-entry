import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { FormBuilder } from '@angular/forms';
import { FormValidatorService } from '../form-validator.service';
import { LocalService } from '../local.service';
import { AccountListsService } from '../account-lists.service';
import { SessionService } from '../session.service';
import { Router, ActivatedRoute } from '@angular/router';
import { BalancesService } from '../server/api/api';
import { forkJoin } from 'rxjs';
import { AccountType } from '../account-type';

@Component({
  selector: 'app-earnings',
  templateUrl: './earnings.component.html',
  styleUrls: ['./earnings.component.scss']
})
export class EarningsComponent implements OnInit {

  accountType: AccountType;
  earnings: any[];

  showErrors = false;

  form = this.fb.group({
    after: [this.defaultAfter, this.fv.date()],
    before: [this.defaultBefore, this.fv.date()],
  });

  @ViewChild('after') afterElement: ElementRef;
  @ViewChild('before') beforeElement: ElementRef;

  constructor(
    private fb: FormBuilder,
    private fv: FormValidatorService,
    private local: LocalService,
    private accountListsService: AccountListsService,
    private sessionService: SessionService,
    private router: Router,
    private balancesService: BalancesService,
    private route: ActivatedRoute
  ) { }

  ngOnInit() {
    this.route.paramMap.subscribe(params => {
      this.accountType = params.get('accountType') == 'expense' ? 
        AccountType.EXPENSE : AccountType.REVENUE;

      this.submit();
    });
  }

  get defaultAfter(): string {
    const today = new Date();
    return this.fromDate(new Date(today.getFullYear(), today.getMonth(), 1));
  }

  get defaultBefore(): string {
    const today = new Date();
    return this.fromDate(new Date(today.getFullYear(), today.getMonth() + 1, 0));
  }

  fromDate(date: Date): string {
    const dateIso = this.local.fromDate(date);
    return this.local.formatDate(dateIso);
  }

  submit() {
    if (this.accountType == null) return;

    if (this.after.invalid) {
      this.showErrors = true;
      return this.afterElement.nativeElement.focus();
    }

    if (this.before.invalid) {
      this.showErrors = true;
      return this.beforeElement.nativeElement.focus();
    }

    this.showErrors = false;

    const after = this.local.parseDate(this.after.value);
    const before = this.local.parseDate(this.before.value);

    forkJoin(
      this.balancesService.balancesFindAll(after, before),
      this.accountListsService.getAccountListsCache()
    )
    .subscribe(([balances, accountListsCache]) => {
      this.earnings = accountListsCache.accountLists.get(this.accountType).entries.map(account => {
        const balance = balances.find(balance => balance.accountId == account.id);

        return {
          name: account.name,
          amount: balance ? balance.balance : 0,
          level: account.hierarchyLevel
        }
      })
    });
  }

  get after() { return this.form.get('after'); }
  get before() { return this.form.get('before'); }
}
