import { Component, OnInit, ViewChild, ElementRef, OnDestroy } from '@angular/core';
import { FormBuilder, FormControl } from '@angular/forms';
import { FormValidatorService } from '../form-validator.service';
import { LocalService } from '../local.service';
import { AccountListsService, Account } from '../account-lists.service';
import { SessionService } from '../session.service';
import { Router, ActivatedRoute } from '@angular/router';
import { BalancesService } from '../server/api/api';
import { forkJoin, Subscription } from 'rxjs';
import { AccountType } from '../account-type';
import { ResponseBalance } from '../server';
import { EarningTimespanType } from '../earning-timespan-type.enum';
import { Earning } from '../earning';

@Component({
  selector: 'app-earnings',
  templateUrl: './earnings.component.html',
  styleUrls: ['./earnings.component.scss']
})
export class EarningsComponent implements OnInit, OnDestroy {

  accountTypes = AccountType;
  timespanTypes = EarningTimespanType;

  accountType: AccountType;

  timespanType = new FormControl(EarningTimespanType.MONTH);
  primaryDate = this.primaryDefault;
  secondaryDate = this.secondaryDefault;

  earnings: Earning[];

  subscription: Subscription;

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

      this.load();
    });

    this.subscription = this.timespanType.valueChanges.subscribe(() => this.load());
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

  load() {
    const [primaryAfter, primaryBefore] = this.createInterval(this.primaryDate);
    const [secondaryAfter, secondaryBefore] = this.createInterval(this.secondaryDate);

    forkJoin(
      this.balancesService.balancesFindAll(primaryAfter, primaryBefore),
      this.balancesService.balancesFindAll(secondaryAfter, secondaryBefore),
      this.accountListsService.getAccountListsCache()
    )
    .subscribe(([primaryBalances, secondaryBalances, accountListsCache]) => {
      const accountList = accountListsCache.accountLists.get(this.accountType);
      const rootAccount = {id: accountList.rootId, name: null, hierarchyLevel: 0};
      const earningAccounts = [rootAccount, ...accountList.entries];

      this.earnings = earningAccounts.map(account => {
        return {
          name: account.name,
          primaryValue: this.findBalance(primaryBalances, account.id),
          secondaryValue: this.findBalance(secondaryBalances, account.id),
          level: account.hierarchyLevel
        };
      });
/*
      const trace = [{earning: this.earning, childIndex: 0}];

      while (trace.length > 0) {
        const current = trace[trace.length - 1];

        if (current.earning.children.length > current.childIndex) {
          trace.push({earning: current.earning.children[current.childIndex], childIndex: 0});
          current.childIndex++;
        } else {
          trace.pop();
          if (trace.length > 0) {
            trace[trace.length - 1].earning.primaryValue += current.earning.primaryValue;
            trace[trace.length - 1].earning.secondaryValue += current.earning.secondaryValue;
          }
        }
      }
      console.log(this.earning)*/
    });
  }

  createInterval(date: Date): string[] {
    const after = this.timespanType.value == EarningTimespanType.MONTH ? 
      new Date(date.getFullYear(), date.getMonth(), 1) :
      new Date(date.getFullYear(), 0, 1);

    const before = this.timespanType.value == EarningTimespanType.MONTH ? 
      new Date(date.getFullYear(), date.getMonth() + 1, 0) :
      new Date(date.getFullYear() + 1, 0, 0);

    return [
      this.local.fromDate(after),
      this.local.fromDate(before)
    ];
  }

  findBalance(balances: ResponseBalance[], accountId: number) {
    const balance = balances.find(balance => balance.accountId == accountId);
    return balance ? balance.balance : 0;
  }

  decrementPrimary() {
    this.primaryDate = this.offsetDate(this.primaryDate, -1);
    this.load();
  }

  incrementPrimary() {
    this.primaryDate = this.offsetDate(this.primaryDate, 1);
    this.load();
  }

  decrementSecondary() {
    this.secondaryDate = this.offsetDate(this.secondaryDate, -1);
    this.load();
  }

  incrementSecondary() {
    this.secondaryDate = this.offsetDate(this.secondaryDate, 1);
    this.load();
  }

  offsetDate(date: Date, offset: number): Date {
    return this.timespanType.value == EarningTimespanType.MONTH ? 
      new Date(date.getFullYear(), date.getMonth() + offset, 1) :
      new Date(date.getFullYear() + offset, 0, 1);
  }

  get primaryDefault(): Date {
    const now = new Date();
    return new Date(now.getFullYear(), now.getMonth(), 1);
  }

  get secondaryDefault(): Date {
    const now = new Date();
    return new Date(now.getFullYear(), now.getMonth() - 1, 1);
  }

}
