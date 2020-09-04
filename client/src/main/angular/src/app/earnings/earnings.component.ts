import { Component, OnInit, OnDestroy } from '@angular/core';
import { AccountsService } from '../generated/openapi/api/accounts.service';
import { AccountHierarchyService } from '../account-hierarchy/account-hierarchy.service';
import { BalancesService } from '../generated/openapi/api/balances.service';
import * as moment from 'moment';
import { AccountHierarchy, AccountType } from '../account-hierarchy/account-hierarchy';
import { forkJoin, Subscription, combineLatest, of } from 'rxjs';
import { switchMap, distinctUntilChanged, map } from 'rxjs/operators';
import { API_DATE } from '../api-access/api-constants';
import { GetBalanceResponse, Account } from '../generated/openapi/model/models';
import { ApiErrorHandlerService } from '../api-access/api-error-handler.service';
import { DialogService } from '../dialogs/dialog.service';
import { DialogMessage } from '../dialogs/dialog-message.enum';
import { Router, ActivatedRoute, ParamMap } from '@angular/router';
import { IntervalType } from './interval-type.enum';
import { ViewEarning } from './view-earning';
import { FormControl } from '@angular/forms';
import { SearchRequest } from './search-request';
import { Interval } from './interval';

@Component({
  selector: 'app-earnings',
  templateUrl: './earnings.component.html',
  styleUrls: ['./earnings.component.scss']
})
export class EarningsComponent implements OnInit, OnDestroy {
  IntervalType = IntervalType;
  AccountType = AccountType;

  accountType: AccountType;
  intervalType = new FormControl();
  intervals: Interval[];

  totals: number[];
  earnings: ViewEarning[];
  accountHierarchy: AccountHierarchy;
  selection: ViewEarning;

  ignoreEvents = true;
  subscription = new Subscription();
  
  constructor(
    private accountsService: AccountsService,
    private accountHierarchyService: AccountHierarchyService,
    private balancesService: BalancesService,
    private apiErrorHandlerService: ApiErrorHandlerService,
    private dialogService: DialogService,
    private router: Router,
    private activatedRoute: ActivatedRoute,
  ) { }

  ngOnInit(): void {
    this.subscription.add(combineLatest(
      this.activatedRoute.paramMap,
      this.activatedRoute.queryParamMap
    ).pipe(
      map(([param, query]) => this.parseSearchRequest(param, query)),
      switchMap(searchRequest => {
        return forkJoin([
          of(searchRequest),
          this.accountsService.getAccounts(),
          this.balancesService.getBalances(...this.getDatesForInterval(searchRequest.intervals[0])),
          this.balancesService.getBalances(...this.getDatesForInterval(searchRequest.intervals[1])),
        ]);
      })
    ).subscribe(
      ([searchRequest, accounts, ...balances]) => this.load(searchRequest, accounts, balances),
      error => this.apiErrorHandlerService.handle(error)
    ));

    this.subscription.add(this.intervalType.valueChanges.subscribe(() => this.onIntervalTypeChanged()));
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

  private parseSearchRequest(param: ParamMap, query: ParamMap): SearchRequest {
    return {
      intervals: [this.parseInterval(query, 0), this.parseInterval(query, 1)],
      accountType: this.parseAccountType(param),
    }
  }

  private parseInterval(query: ParamMap, index: number): Interval {
    const month = query.get('month' + index);
    const year = query.get('year' + index);
    
    if (!year) {
      return { 
        month: moment().month() - 1 + index, 
        year: moment().year() 
      };
    } else {
      return { 
        month: month ? +month : null, 
        year: +year 
      };
    }
  }

  private parseAccountType(param: ParamMap): AccountType {
    const accountType = AccountType[param.get('accountType')];
    return accountType ? accountType : AccountType.EXPENSE;
  }

  private getDatesForInterval(interval: Interval): string[] {
    if (interval.month) {
      return [
        moment([interval.year, interval.month - 1]).startOf('month').format(API_DATE),
        moment([interval.year, interval.month - 1]).endOf('month').format(API_DATE),
      ];
    } else {
      return [
        moment([interval.year]).startOf('year').format(API_DATE),
        moment([interval.year]).endOf('year').format(API_DATE),
      ];
    }
  }

  load(searchRequest: SearchRequest, accounts: Account[], balances: GetBalanceResponse[][]) {
    this.ignoreEvents = true;
    this.accountType = searchRequest.accountType;
    this.intervalType.setValue(searchRequest.intervals[0].month ? IntervalType.MONTH : IntervalType.YEAR);
    this.intervals = searchRequest.intervals;
    this.accountHierarchy = this.accountHierarchyService.createAccountHierarchy(accounts);

    const balancesById = [this.getBalancesById(balances[0]), this.getBalancesById(balances[1])];
    this.earnings = [];
    this.totals = [0, 0];

    for (let account of this.accountHierarchy.list.get(this.accountType)) {
      if (account.id == this.accountHierarchy.root.get(this.accountType).id) {
        this.totals[0] = this.getOrZero(balancesById[0].get(account.id));
        this.totals[1] = this.getOrZero(balancesById[1].get(account.id));
      } else {
        const amounts = [ balancesById[0].get(account.id), balancesById[1].get(account.id) ];

        if (amounts[0] != null || amounts[1] != null) {
          this.earnings.push({account, balances: [
            { interval: this.intervals[0], amount: this.getOrZero(amounts[0]) },
            { interval: this.intervals[1], amount: this.getOrZero(amounts[1]) },
          ]});
        }
      }
    }
    this.ignoreEvents = false;
  }

  private getOrZero(value?: number) {
    return value != null ? value : 0;
  }

  private getBalancesById(balances: GetBalanceResponse[]): Map<number, number> {
    const balancesById = new Map<number, number>();

    for (let balance of balances) {
      balancesById.set(balance.accountId, balance.balance);
    }
    
    return balancesById;
  }

  onIntervalTypeChanged() {
    if (this.ignoreEvents) return;
    
    for (let i = 0; i < this.intervals.length; i++) {
      this.intervals[i].month = this.intervalType.value == IntervalType.MONTH ? i + 1 : null;
    }

    this.updateQuery();
  }

  incrementInterval(interval: Interval) {
    if (interval.month) {
      interval.month++;
      if (interval.month > 12) {
        interval.month = 1;
        interval.year++;
      }
    } else {
      interval.year++;
    }

    this.updateQuery();
  }

  decrementInterval(interval: Interval) {
    if (interval.month) {
      interval.month--;
      if (interval.month < 1) {
        interval.month = 12;
        interval.year--;
      }
    } else {
      interval.year--;
    }

    this.updateQuery();
  }

  private updateQuery() {
    this.router.navigate(['/earnings', this.accountType], {
      replaceUrl: true,
      queryParams: {
        year0: this.intervals[0].year,
        month0: this.intervals[0].month,
        year1: this.intervals[1].year,
        month1: this.intervals[1].month,
      }
    });
  }

  select(earning: ViewEarning) {
    this.selection = earning;
  }

  setActive(active: boolean) {
    this.accountsService.updateAccount(this.selection.account.id, {
      name: this.selection.account.name,
      parentId: this.selection.account.parentId,
      active
    }).subscribe(
      () => {
        const message = active ? DialogMessage.ACCOUNT_ACTIVATED : DialogMessage.ACCOUNT_DEACTIVATED;
        this.dialogService.show(message, {name: this.selection.account.name});
        this.selection.account.active = active;
        this.selection = null;
      },
      error => this.apiErrorHandlerService.handle(error),
    )
  }

  showTransactions(interval: Interval) {
    const dates = this.getDatesForInterval(interval);

    this.router.navigate(['/transactions'], {queryParams: {
      account: this.selection.account.id,
      type: interval.month ? 'month' : 'year',
      ...interval
    }});
  }
}
